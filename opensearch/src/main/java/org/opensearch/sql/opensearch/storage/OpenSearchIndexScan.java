/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.opensearch.storage;

import static org.opensearch.search.sort.FieldSortBuilder.DOC_FIELD_NAME;
import static org.opensearch.search.sort.SortOrder.ASC;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortBuilder;
import org.opensearch.sql.common.setting.Settings;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.FunctionExpression;
import org.opensearch.sql.expression.ReferenceExpression;
import org.opensearch.sql.opensearch.client.OpenSearchClient;
import org.opensearch.sql.opensearch.data.value.OpenSearchExprValueFactory;
import org.opensearch.sql.opensearch.request.OpenSearchQueryRequest;
import org.opensearch.sql.opensearch.request.OpenSearchRequest;
import org.opensearch.sql.opensearch.response.OpenSearchResponse;
import org.opensearch.sql.opensearch.response.agg.OpenSearchAggregationResponseParser;
import org.opensearch.sql.opensearch.s3.S3ObjectMetaData;
import org.opensearch.sql.opensearch.s3.S3Scan;
import org.opensearch.sql.storage.TableScanOperator;
import org.opensearch.sql.storage.bindingtuple.BindingTuple;

/**
 * OpenSearch index scan operator.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class OpenSearchIndexScan extends TableScanOperator {

  private static final Logger log = LogManager.getLogger(OpenSearchIndexScan.class);

  /**
   * OpenSearch client.
   */
  private final OpenSearchClient client;

  /**
   * Search request.
   */
  @EqualsAndHashCode.Include
  @Getter
  @ToString.Include
  private final OpenSearchRequest request;

  /**
   * Search response for current batch.
   */
  private Iterator<ExprValue> iterator;

  @Getter
  private boolean isS3Scan;
  private int s3Limit = 200;
  private int s3Offset = 0;

  /**
   * Constructor.
   */
  public OpenSearchIndexScan(OpenSearchClient client,
                             Settings settings, String indexName,
                             OpenSearchExprValueFactory exprValueFactory) {
    this(client, settings, new OpenSearchRequest.IndexName(indexName), exprValueFactory);
  }

  /**
   * Constructor.
   */
  public OpenSearchIndexScan(OpenSearchClient client,
                             Settings settings, OpenSearchRequest.IndexName indexName,
                             OpenSearchExprValueFactory exprValueFactory) {
    if (Arrays.stream(indexName.getIndexNames())
        .anyMatch(name -> name.startsWith("s3-") && name.endsWith("-metadata"))) {
      isS3Scan = true;
    }
    this.client = client;
    this.request = new OpenSearchQueryRequest(indexName,
        settings.getSettingValue(Settings.Key.QUERY_SIZE_LIMIT), exprValueFactory);
  }

  @Override
  public void open() {
    super.open();

    // For now pull all results immediately once open
    List<OpenSearchResponse> responses = new ArrayList<>();
    OpenSearchResponse response = client.search(request);
    while (!response.isEmpty()) {
      responses.add(response);
      response = client.search(request);
    }
    if (!isS3Scan) {
      iterator = Iterables.concat(responses.toArray(new OpenSearchResponse[0])).iterator();
      return;
    }

    Iterator<ExprValue> logStream =
        Iterables.concat(responses.toArray(new OpenSearchResponse[0])).iterator();
    S3Scan s3Scan = new S3Scan(s3Objects(logStream));
    s3Scan.open();
    Iterators.advance(s3Scan, s3Offset);
    iterator = Iterators.limit(s3Scan, s3Limit);
  }

  private List<S3ObjectMetaData> s3Objects(Iterator<ExprValue> logStream) {
    List<S3ObjectMetaData> s3Objects = new ArrayList<>();
    logStream.forEachRemaining(value -> {
      final BindingTuple tuple = value.bindingTuples();
      final ExprValue bucket =
          tuple.resolve(new ReferenceExpression("meta.bucket", ExprCoreType.STRING));
      final ExprValue object =
          tuple.resolve(new ReferenceExpression("meta.object", ExprCoreType.STRING));
      log.info("bucket {}, object {}", bucket.stringValue(), object.stringValue());
      s3Objects.add(new S3ObjectMetaData(bucket.stringValue(), object.stringValue()));
    });
    return s3Objects;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }

  public void pushDownS3Limit(int limit, int offset) {
    this.s3Limit = limit;
    this.s3Offset = offset;
  }

  public void pushDownS3TimeFilters(Expression filter) {
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
    pushDown(query);
  }

  /**
   * Push down query to DSL request.
   *
   * @param query query request
   */
  public void pushDown(QueryBuilder query) {
    SearchSourceBuilder source = request.getSourceBuilder();
    QueryBuilder current = source.query();

    if (current == null) {
      source.query(query);
    } else {
      if (isBoolFilterQuery(current)) {
        ((BoolQueryBuilder) current).filter(query);
      } else {
        source.query(QueryBuilders.boolQuery()
            .filter(current)
            .filter(query));
      }
    }

    if (source.sorts() == null) {
      source.sort(DOC_FIELD_NAME, ASC); // Make sure consistent order
    }
  }

  /**
   * Push down aggregation to DSL request.
   *
   * @param aggregationBuilder pair of aggregation query and aggregation parser.
   */
  public void pushDownAggregation(
      Pair<List<AggregationBuilder>, OpenSearchAggregationResponseParser> aggregationBuilder) {
    SearchSourceBuilder source = request.getSourceBuilder();
    aggregationBuilder.getLeft().forEach(builder -> source.aggregation(builder));
    source.size(0);
    request.getExprValueFactory().setParser(aggregationBuilder.getRight());
  }

  /**
   * Push down sort to DSL request.
   *
   * @param sortBuilders sortBuilders.
   */
  public void pushDownSort(List<SortBuilder<?>> sortBuilders) {
    SearchSourceBuilder source = request.getSourceBuilder();
    for (SortBuilder<?> sortBuilder : sortBuilders) {
      source.sort(sortBuilder);
    }
  }

  /**
   * Push down size (limit) and from (offset) to DSL request.
   */
  public void pushDownLimit(Integer limit, Integer offset) {
    SearchSourceBuilder sourceBuilder = request.getSourceBuilder();
    sourceBuilder.from(offset).size(limit);
  }

  /**
   * Push down project list to DSL requets.
   */
  public void pushDownProjects(Set<ReferenceExpression> projects) {
    SearchSourceBuilder sourceBuilder = request.getSourceBuilder();
    final Set<String> projectsSet =
        projects.stream().map(ReferenceExpression::getAttr).collect(Collectors.toSet());
    sourceBuilder.fetchSource(projectsSet.toArray(new String[0]), new String[0]);
  }

  public void pushTypeMapping(Map<String, ExprType> typeMapping) {
    request.getExprValueFactory().setTypeMapping(typeMapping);
  }

  @Override
  public void close() {
    super.close();

    client.cleanup(request);
  }

  private boolean isBoolFilterQuery(QueryBuilder current) {
    return (current instanceof BoolQueryBuilder);
  }

  @Override
  public String explain() {
    return getRequest().toString();
  }
}
