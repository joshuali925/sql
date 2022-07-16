/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.ast.expression;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.opensearch.sql.ast.AbstractNodeVisitor;

/**
 * Represent the assign operation. e.g. velocity = distance/speed.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class RowFormatSerDe extends UnresolvedExpression {
  private final RowFormat rowFormat;

  public RowFormatSerDe(String format) {
    this.rowFormat = RowFormat.valueOf(format.toUpperCase());
  }

  @Override
  public List<UnresolvedExpression> getChild() {
    return ImmutableList.of();
  }

  @Override
  public <R, C> R accept(AbstractNodeVisitor<R, C> nodeVisitor, C context) {
    return nodeVisitor.visitRowFormatSerDe(this, context);
  }

  public enum RowFormat {
    JSON,
    REGEX;
  }
}
