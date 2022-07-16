/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.ast.tree;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.opensearch.sql.ast.AbstractNodeVisitor;
import org.opensearch.sql.ast.expression.Literal;
import org.opensearch.sql.ast.expression.RowFormatSerDe;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class CreateTable extends UnresolvedPlan {
  private final String tableName;
  private final Map<String, String> columns;
  private final RowFormatSerDe rowFormatSerDe;
  private final Map<String, String> rowFormatSerDeProperties;
  private final Literal partitionBy;
  private final Literal location;

  @Override
  public UnresolvedPlan attach(UnresolvedPlan child) {
    return this;
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitCreateTable(this, context);
  }

  @Override
  public List<UnresolvedPlan> getChild() {
    return ImmutableList.of();
  }
}
