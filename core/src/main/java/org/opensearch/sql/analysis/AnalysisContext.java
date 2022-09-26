/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.NamedExpression;

/**
 * The context used for Analyzer.
 */
public class AnalysisContext {
  /**
   * Environment stack for symbol scope management.
   */
  private TypeEnvironment environment;
  @Getter
  private final List<NamedExpression> namedParseExpressions;

  /**
   * Storage for values of functions which return a constant value.
   * We are storing the values there to use it in sequential calls to those functions.
   * For example, `now` function should the same value during processing a query.
   */
  @Getter
  private final Map<String, Expression> constantFunctionValues;

  public AnalysisContext() {
    this(new TypeEnvironment(null));
  }

  /**
   * Class CTOR.
   * @param environment Env to set to a new instance.
   */
  public AnalysisContext(TypeEnvironment environment) {
    this.environment = environment;
    this.namedParseExpressions = new ArrayList<>();
    this.constantFunctionValues = new HashMap<>();
  }

  /**
   * Push a new environment.
   */
  public void push() {
    environment = new TypeEnvironment(environment);
  }

  /**
   * Return current environment.
   *
   * @return current environment
   */
  public TypeEnvironment peek() {
    return environment;
  }

  /**
   * Pop up current environment from environment chain.
   *
   * @return current environment (before pop)
   */
  public TypeEnvironment pop() {
    Objects.requireNonNull(environment, "Fail to pop context due to no environment present");

    TypeEnvironment curEnv = environment;
    environment = curEnv.getParent();
    return curEnv;
  }
}
