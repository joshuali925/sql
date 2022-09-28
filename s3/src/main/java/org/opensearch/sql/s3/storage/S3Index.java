/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.s3.storage;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.physical.PhysicalPlan;
// import org.opensearch.sql.prometheus.client.PrometheusClient;
// import org.opensearch.sql.prometheus.config.PrometheusConfig;
import org.opensearch.sql.s3.client.S3ClientImpl;
import org.opensearch.sql.storage.Table;

/**
 * OpenSearch table (index) implementation.
 */
public class S3Index implements Table {

  private final S3ClientImpl s3ClientImpl;

  /**
   * The cached mapping of field and type in index.
   */
  private Map<String, ExprType> cachedFieldTypes = null;

  /**
   * Constructor.
   */
  public S3Index(S3ClientImpl s3ClientImpl) {
    this.s3ClientImpl = s3ClientImpl;
  }


  /*
   * TODO: Assume indexName doesn't have wildcard.
   *  Need to either handle field name conflicts
   *   or lazy evaluate when query engine pulls field type.
   */
  @Override
  public Map<String, ExprType> getFieldTypes() {
    if (cachedFieldTypes == null) {
      // cachedFieldTypes = new OpenSearchDescribeIndexRequest(client, indexName).getFieldTypes();
      cachedFieldTypes = ImmutableMap.of("raw", ExprCoreType.STRING);
    }
    return cachedFieldTypes;
  }

  /**
   * TODO: Push down operations to index scan operator as much as possible in future.
   */
  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    S3IndexScan indexScan =
        new S3IndexScan(prometheusService, prometheusConfig, metricName,
            new PrometheusExprValueFactory(getFieldTypes()));

    /*
     * Visit logical plan with index scan as context so logical operators visited, such as
     * aggregation, filter, will accumulate (push down) OpenSearch query and aggregation DSL on
     * index scan.
     */
    return plan.accept(new PrometheusDefaultImplementor(indexScan, prometheusConfig), indexScan);
  }
}