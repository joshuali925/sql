/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.opensearch.storage;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilder;
import org.opensearch.sql.common.setting.Settings;
import org.opensearch.sql.common.utils.StringUtils;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.FunctionExpression;
import org.opensearch.sql.opensearch.client.OpenSearchClient;
import org.opensearch.sql.opensearch.data.value.OpenSearchExprValueFactory;
import org.opensearch.sql.opensearch.planner.logical.OpenSearchLogicalIndexAgg;
import org.opensearch.sql.opensearch.planner.logical.OpenSearchLogicalIndexScan;
import org.opensearch.sql.opensearch.planner.logical.OpenSearchLogicalPlanOptimizerFactory;
import org.opensearch.sql.opensearch.planner.physical.ADOperator;
import org.opensearch.sql.opensearch.planner.physical.MLCommonsOperator;
import org.opensearch.sql.opensearch.request.OpenSearchRequest;
import org.opensearch.sql.opensearch.request.system.OpenSearchDescribeIndexRequest;
import org.opensearch.sql.opensearch.response.agg.OpenSearchAggregationResponseParser;
import org.opensearch.sql.opensearch.storage.s3.S3IndexScan;
import org.opensearch.sql.opensearch.storage.script.aggregation.AggregationQueryBuilder;
import org.opensearch.sql.opensearch.storage.script.filter.FilterQueryBuilder;
import org.opensearch.sql.opensearch.storage.script.sort.SortQueryBuilder;
import org.opensearch.sql.opensearch.storage.serialization.DefaultExpressionSerializer;
import org.opensearch.sql.planner.DefaultImplementor;
import org.opensearch.sql.planner.logical.LogicalAD;
import org.opensearch.sql.planner.logical.LogicalMLCommons;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.logical.LogicalRelation;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.storage.Table;

/**
 * OpenSearch table (index) implementation.
 */
public class OpenSearchIndex implements Table {

  /**
   * OpenSearch client connection.
   */
  private final OpenSearchClient client;

  private final Settings settings;

  /**
   * {@link OpenSearchRequest.IndexName}.
   */
  private final OpenSearchRequest.IndexName indexName;

  /**
   * The cached mapping of field and type in index.
   */
  private Map<String, ExprType> cachedFieldTypes = null;

  /**
   * Constructor.
   */
  public OpenSearchIndex(OpenSearchClient client, Settings settings, String indexName) {
    this.client = client;
    this.settings = settings;
    this.indexName = new OpenSearchRequest.IndexName(indexName);
  }

  /*
   * TODO: Assume indexName doesn't have wildcard.
   *  Need to either handle field name conflicts
   *   or lazy evaluate when query engine pulls field type.
   */
  @Override
  public Map<String, ExprType> getFieldTypes() {
    if (cachedFieldTypes == null) {
      cachedFieldTypes = new OpenSearchDescribeIndexRequest(client, indexName).getFieldTypes();
    }
    return cachedFieldTypes;
  }

  /**
   * TODO: Push down operations to index scan operator as much as possible in future.
   */
  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    OpenSearchIndexScan indexScan;
    if (Arrays.stream(indexName.getIndexNames())
        .anyMatch(name -> name.startsWith("s3-") && name.endsWith("-metadata"))) {
      indexScan = new S3IndexScan(client, settings, indexName,
          new OpenSearchExprValueFactory(getFieldTypes()));
    } else {
      indexScan = new OpenSearchIndexScan(client, settings, indexName,
          new OpenSearchExprValueFactory(getFieldTypes()));
    }

    /*
     * Visit logical plan with index scan as context so logical operators visited, such as
     * aggregation, filter, will accumulate (push down) OpenSearch query and aggregation DSL on
     * index scan.
     */
    return plan.accept(new OpenSearchDefaultImplementor(indexScan, client), indexScan);
  }

  @Override
  public LogicalPlan optimize(LogicalPlan plan) {
    return OpenSearchLogicalPlanOptimizerFactory.create().optimize(plan);
  }

  @VisibleForTesting
  @RequiredArgsConstructor
  public static class OpenSearchDefaultImplementor
      extends DefaultImplementor<OpenSearchIndexScan> {
    private final OpenSearchIndexScan indexScan;

    private final OpenSearchClient client;

    @Override
    public PhysicalPlan visitNode(LogicalPlan plan, OpenSearchIndexScan context) {
      if (plan instanceof OpenSearchLogicalIndexScan) {
        return visitIndexScan((OpenSearchLogicalIndexScan) plan, context);
      } else if (plan instanceof OpenSearchLogicalIndexAgg) {
        return visitIndexAggregation((OpenSearchLogicalIndexAgg) plan, context);
      } else {
        throw new IllegalStateException(StringUtils.format("unexpected plan node type %s",
            plan.getClass()));
      }
    }

    public QueryBuilder pushDownS3TimeFilters(Expression filter) {
      String start =
          ((FunctionExpression) ((FunctionExpression) filter).getArguments()
              .get(0)).getArguments().get(1).valueOf(null).timestampValue().toString();
      String end =
          ((FunctionExpression) ((FunctionExpression) filter).getArguments()
              .get(1)).getArguments().get(1).valueOf(null).timestampValue().toString();

      BoolQueryBuilder left = QueryBuilders.boolQuery();
      left.must(QueryBuilders.rangeQuery("meta.startTime").lte(start));
      left.must(QueryBuilders.rangeQuery("meta.endTime").gte(start));
      BoolQueryBuilder right = QueryBuilders.boolQuery();
      right.must(QueryBuilders.rangeQuery("meta.startTime").lte(end));
      right.must(QueryBuilders.rangeQuery("meta.endTime").gte(end));
      BoolQueryBuilder center = QueryBuilders.boolQuery();
      center.must(QueryBuilders.rangeQuery("meta.startTime").gte(start));
      center.must(QueryBuilders.rangeQuery("meta.endTime").lte(end));

      BoolQueryBuilder query = QueryBuilders.boolQuery();
      query.should(left).should(right).should(center);
      return query;
    }

    /**
     * Implement ElasticsearchLogicalIndexScan.
     */
    public PhysicalPlan visitIndexScan(OpenSearchLogicalIndexScan node,
                                       OpenSearchIndexScan context) {
      if (null != node.getSortList()) {
        final SortQueryBuilder builder = new SortQueryBuilder();
        context.pushDownSort(node.getSortList().stream()
            .map(sort -> builder.build(sort.getValue(), sort.getKey()))
            .collect(Collectors.toList()));
      }

      if (null != node.getFilter()) {
        if (context instanceof S3IndexScan) {
          context.pushDown(pushDownS3TimeFilters(node.getFilter()));
        }
        FilterQueryBuilder queryBuilder = new FilterQueryBuilder(new DefaultExpressionSerializer());
        QueryBuilder query = queryBuilder.build(node.getFilter());
        context.pushDown(query);
      }

      if (node.getLimit() != null) {
        context.pushDownLimit(node.getLimit(), node.getOffset());
      }

      if (node.hasProjects()) {
        context.pushDownProjects(node.getProjectList());
      }
      return indexScan;
    }

    /**
     * Implement ElasticsearchLogicalIndexAgg.
     */
    public PhysicalPlan visitIndexAggregation(OpenSearchLogicalIndexAgg node,
                                              OpenSearchIndexScan context) {
      if (node.getFilter() != null) {
        FilterQueryBuilder queryBuilder = new FilterQueryBuilder(
            new DefaultExpressionSerializer());
        QueryBuilder query = queryBuilder.build(node.getFilter());
        context.pushDown(query);
      }
      AggregationQueryBuilder builder =
          new AggregationQueryBuilder(new DefaultExpressionSerializer());
      Pair<List<AggregationBuilder>, OpenSearchAggregationResponseParser> aggregationBuilder =
          builder.buildAggregationBuilder(node.getAggregatorList(),
              node.getGroupByList(), node.getSortList());
      context.pushDownAggregation(aggregationBuilder);
      context.pushTypeMapping(
          builder.buildTypeMapping(node.getAggregatorList(),
              node.getGroupByList()));
      return indexScan;
    }

    @Override
    public PhysicalPlan visitRelation(LogicalRelation node, OpenSearchIndexScan context) {
      return indexScan;
    }

    @Override
    public PhysicalPlan visitMLCommons(LogicalMLCommons node, OpenSearchIndexScan context) {
      return new MLCommonsOperator(visitChild(node, context), node.getAlgorithm(),
          node.getArguments(), client.getNodeClient());
    }

    @Override
    public PhysicalPlan visitAD(LogicalAD node, OpenSearchIndexScan context) {
      return new ADOperator(visitChild(node, context),
          node.getArguments(), client.getNodeClient());
    }
  }
}
