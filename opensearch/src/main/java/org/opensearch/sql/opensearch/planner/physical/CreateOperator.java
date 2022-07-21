/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.opensearch.planner.physical;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.ast.expression.RowFormatSerDe;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.opensearch.client.OpenSearchClient;
import org.opensearch.sql.opensearch.index.SqlMetadataIndex;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.planner.physical.PhysicalPlanNodeVisitor;

/**
 * ml-commons Physical operator to call machine learning interface to get results for
 * algorithm execution.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreateOperator extends PhysicalPlan {
  private static final Logger log = LogManager.getLogger(CreateOperator.class);

  @Getter
  private final String tableName;

  @Getter
  private final Map<String, String> columns;
  private final RowFormatSerDe rowFormatSerDe;
  private final Map<String, String> rowFormatSerDeProperties;
  private final String partitionBy;
  private final String location;
  private SqlMetadataIndex sqlMetadataIndex;

  @Getter
  private final OpenSearchClient client;

  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  private final List<ExprValue> responses = new ArrayList<>();

  @Override
  public void open() {
    super.open();
    sqlMetadataIndex = new SqlMetadataIndex(client);
    putTableMetadata();
    iterator = responses.iterator();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitCreate(this, context);
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

  @Override
  public ExecutionEngine.Schema schema() {
    return new ExecutionEngine.Schema(ImmutableList.of(new ExecutionEngine.Schema.Column("response",
        null, ExprCoreType.STRING)));
  }

  private void putTableMetadata() {
    if (sqlMetadataIndex.tableExists(tableName)) {
      addResponse(String.format("Table `%s` already exists.", tableName));
      return;
    }

    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.put("type", "s3")
        .put("name", tableName)
        .put("columns", columns)
        .put("rowFormatSerde", rowFormatSerDe.getRowFormat().toString());
    Optional.ofNullable(rowFormatSerDeProperties)
        .ifPresent(v -> builder.put("rowFormatSerdeProperties", v));
    Optional.ofNullable(partitionBy).ifPresent(v -> builder.put("partitionBy", v));
    builder.put("location", location);

    sqlMetadataIndex.createTable(tableName, builder.build());
    addResponse(String.format("Created table `%s`.", tableName));
  }

  private void addResponse(String message) {
    responses.add(
        ExprTupleValue.fromExprValueMap(ImmutableMap.of("response", new ExprStringValue(message))));
  }

}
