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
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.planner.physical.PhysicalPlanNodeVisitor;

/**
 * Drop table Physical operator to delete table metadata documents.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DropOperator extends PhysicalPlan {
  private static final Logger log = LogManager.getLogger(DropOperator.class);

  @Getter
  private final String tableName;

  @Getter
  private final OpenSearchClient client;

  private SqlMetadataIndex sqlMetadataIndex;

  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  private final List<ExprValue> responses = new ArrayList<>();

  @Override
  public void open() {
    super.open();
    sqlMetadataIndex = new SqlMetadataIndex(client);
    try {
      sqlMetadataIndex.dropTable(tableName);
      addResponse(String.format("Deleted table `%s`.", tableName));
    } catch (IllegalStateException e) {
      addResponse(e.getMessage());
    }
    iterator = responses.iterator();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitDrop(this, context);
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

  private void addResponse(String message) {
    responses.add(
        ExprTupleValue.fromExprValueMap(ImmutableMap.of("response", new ExprStringValue(message))));
  }

}
