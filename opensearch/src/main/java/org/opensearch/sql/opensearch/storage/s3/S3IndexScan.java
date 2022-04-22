package org.opensearch.sql.opensearch.storage.s3;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilder;
import org.opensearch.search.sort.SortBuilder;
import org.opensearch.sql.common.setting.Settings;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.FunctionExpression;
import org.opensearch.sql.expression.ReferenceExpression;
import org.opensearch.sql.opensearch.client.OpenSearchClient;
import org.opensearch.sql.opensearch.data.value.OpenSearchExprValueFactory;
import org.opensearch.sql.opensearch.request.OpenSearchRequest;
import org.opensearch.sql.opensearch.response.OpenSearchResponse;
import org.opensearch.sql.opensearch.response.agg.OpenSearchAggregationResponseParser;
import org.opensearch.sql.opensearch.storage.OpenSearchIndexScan;
import org.opensearch.sql.storage.bindingtuple.BindingTuple;

public class S3IndexScan extends OpenSearchIndexScan {
  private static final Logger log = LogManager.getLogger(S3IndexScan.class);
  private int s3Limit = 200;
  private int s3Offset = 0;

  public S3IndexScan(OpenSearchClient client,
                     Settings settings, OpenSearchRequest.IndexName indexName,
                     OpenSearchExprValueFactory exprValueFactory) {
    super(client, settings, indexName, exprValueFactory);
  }

  @Override
  public void open() {
    super.open();
    S3Scan s3Scan = new S3Scan(s3Objects(super.iterator));
    s3Scan.open();
    Iterators.advance(s3Scan, s3Offset);
    iterator = Iterators.limit(s3Scan, s3Limit);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
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
  public void pushDownLimit(Integer limit, Integer offset) {
    this.s3Limit = limit;
    this.s3Offset = offset;
  }

  @Override
  public void pushDown(QueryBuilder query) {
    // super.pushDown(query);
  }

  @Override
  public void pushDownAggregation(
      Pair<List<AggregationBuilder>, OpenSearchAggregationResponseParser> aggregationBuilder) {
    // super.pushDownAggregation(aggregationBuilder);
  }

  @Override
  public void pushDownSort(List<SortBuilder<?>> sortBuilders) {
    // super.pushDownSort(sortBuilders);
  }

  @Override
  public void pushDownProjects(Set<ReferenceExpression> projects) {
    // super.pushDownProjects(projects);
  }
}
