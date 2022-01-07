/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.planner.logical;

/**
 * The visitor of {@link LogicalPlan}.
 *
 * @param <R> return object type.
 * @param <C> context type.
 */
public abstract class LogicalPlanNodeVisitor<R, C> {

  public R visitNode(LogicalPlan plan, C context) {
    return null;
  }

  public R visitRelation(LogicalRelation plan, C context) {
    return visitNode(plan, context);
  }

  public R visitFilter(LogicalFilter plan, C context) {
    return visitNode(plan, context);
  }

  public R visitAggregation(LogicalAggregation plan, C context) {
    return visitNode(plan, context);
  }

  public R visitDedupe(LogicalDedupe plan, C context) {
    return visitNode(plan, context);
  }

  public R visitRename(LogicalRename plan, C context) {
    return visitNode(plan, context);
  }

  public R visitProject(LogicalProject plan, C context) {
    return visitNode(plan, context);
  }

  public R visitWindow(LogicalWindow plan, C context) {
    return visitNode(plan, context);
  }

  public R visitRemove(LogicalRemove plan, C context) {
    return visitNode(plan, context);
  }

  public R visitEval(LogicalEval plan, C context) {
    return visitNode(plan, context);
  }

  public R visitSort(LogicalSort plan, C context) {
    return visitNode(plan, context);
  }

  public R visitValues(LogicalValues plan, C context) {
    return visitNode(plan, context);
  }

  public R visitRareTopN(LogicalRareTopN plan, C context) {
    return visitNode(plan, context);
  }

  public R visitLimit(LogicalLimit plan, C context) {
    return visitNode(plan, context);
  }

  public R visitParse(LogicalParse plan, C context) {
    return visitNode(plan, context);
  }
}
