/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.expression.env.Environment;
import org.opensearch.sql.utils.ParseUtils;

/**
 * Named argument expression that represents function argument with name.
 */
@EqualsAndHashCode
@ToString
public class ParseExpression implements Expression {
  private static final Logger log = LogManager.getLogger(ParseExpression.class);

  @Getter
  private final Expression expression;
  private final String rawPattern;
  @Getter
  private final String identifier;
  @Getter
  @EqualsAndHashCode.Exclude
  private final Pattern pattern;

  /**
   * ParseExpression.
   *
   * @param expression text field
   * @param rawPattern regex
   * @param identifier regex group to extract
   */
  public ParseExpression(Expression expression, String rawPattern, String identifier) {
    this.expression = expression;
    this.rawPattern = rawPattern;
    this.identifier = identifier;
    this.pattern = Pattern.compile(rawPattern);
  }

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    return ParseUtils.parseValue(valueEnv.resolve(expression), pattern, identifier);
  }

  @Override
  public ExprType type() {
    return ExprCoreType.STRING;
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitParse(this, context);
  }
}
