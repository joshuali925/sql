/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.opensearch.planner.physical;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionFuture;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.create.CreateIndexResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.rest.RestStatus;
import org.opensearch.sql.ast.expression.Literal;
import org.opensearch.sql.ast.expression.RowFormatSerDe;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.opensearch.client.OpenSearchClient;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.planner.physical.PhysicalPlanNodeVisitor;

/**
 * ml-commons Physical operator to call machine learning interface to get results for
 * algorithm execution.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreateTableOperator extends PhysicalPlan {
  private static final Logger log = LogManager.getLogger(CreateTableOperator.class);
  private static final String INDEX_NAME = ".opensearch-sql";

  @Getter
  private final String tableName;

  @Getter
  private final Map<String, String> columns;
  private final RowFormatSerDe rowFormatSerDe;
  private final Map<String, String> rowFormatSerDeProperties;
  private final String partitionBy;
  private final String location;

  @Getter
  private final OpenSearchClient client;

  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  private final List<ExprValue> responses = new ArrayList<>();

  @Override
  public void open() {
    super.open();
    putTableMetadata();
    iterator = responses.iterator();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitCreateTable(this, context);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return ImmutableList.of();
  }

  private void createIndex(String indexName) {
    if (!client.indexExists(indexName)) {
      CreateIndexRequest request = new CreateIndexRequest(indexName);
      ActionFuture<CreateIndexResponse> actionFuture =
          client.getNodeClient().admin().indices().create(request);
      CreateIndexResponse response = actionFuture.actionGet();
      if (response.isAcknowledged()) {
        log.info("Acknowledged " + indexName + " creation.");
      } else {
        throw new IllegalStateException("Failed to create index " + indexName);
      }
    }
  }

  private void putTableMetadata() {
    createIndex(INDEX_NAME);
    Map<String, Object> valueMap = new LinkedHashMap<>();
    valueMap.put("type", "s3");
    valueMap.put("name", tableName);
    valueMap.put("columns", columns);
    valueMap.put("rowFormatSerde", rowFormatSerDe.getRowFormat().toString());
    Optional.ofNullable(rowFormatSerDeProperties).ifPresent(v -> valueMap.put("rowFormatSerdeProperties", v));
    Optional.ofNullable(partitionBy).ifPresent(v -> valueMap.put("partitionBy", v));
    valueMap.put("location", location.toString());

    IndexRequest request = new IndexRequest(INDEX_NAME).source(valueMap).create(true);
    ActionFuture<IndexResponse> actionFuture = client.getNodeClient().index(request);
    IndexResponse response = actionFuture.actionGet();
    if (response.status() != RestStatus.CREATED) {
      throw new IllegalStateException("Failed to create table " + tableName);
    }
    responses.add(ExprTupleValue.fromExprValueMap(
        ImmutableMap.of("response", new ExprStringValue("Created table " + tableName))));
  }

  @Override
  public ExecutionEngine.Schema schema() {
    return new ExecutionEngine.Schema(ImmutableList.of(new ExecutionEngine.Schema.Column("response",
        null, ExprCoreType.STRING)));
  }

}
