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
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.opensearch.client.OpenSearchClient;
import org.opensearch.sql.opensearch.index.SqlMetadataIndex;
import org.opensearch.sql.opensearch.index.model.S3MetadataDoc;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.planner.physical.PhysicalPlanNodeVisitor;

/**
 * Create table Physical operator to create sql metadata index and document for table metadata.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CreateOperator extends PhysicalPlan {
  private static final Logger log = LogManager.getLogger(CreateOperator.class);

  @Getter
  private final S3MetadataDoc s3MetadataDoc;
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
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.put("type", "s3")
        .put("name", s3MetadataDoc.getTableName())
        .put("columns", s3MetadataDoc.getColumns())
        .put("rowFormatSerde", s3MetadataDoc.getRowFormatSerDe().getRowFormat().toString());
    Optional.ofNullable(s3MetadataDoc.getRowFormatSerDeProperties())
        .ifPresent(v -> builder.put("rowFormatSerdeProperties", v));
    Optional.ofNullable(s3MetadataDoc.getPartitionBy())
        .ifPresent(v -> builder.put("partitionBy", v));
    builder.put("location", s3MetadataDoc.getLocation());

    if (sqlMetadataIndex.createTable(s3MetadataDoc.getTableName(), builder.build())) {
      addResponse(String.format("Created table `%s`.", s3MetadataDoc.getTableName()));
    } else {
      addResponse(String.format("Failed to create table `%s`, the table might already exist.",
          s3MetadataDoc.getTableName()));
    }
  }

  private void addResponse(String message) {
    responses.add(
        ExprTupleValue.fromExprValueMap(ImmutableMap.of("response", new ExprStringValue(message))));
  }

}
