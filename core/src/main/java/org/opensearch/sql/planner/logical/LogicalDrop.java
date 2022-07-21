package org.opensearch.sql.planner.logical;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * ml-commons logical plan.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalDrop extends LogicalPlan {
  private final String tableName;

  /**
   * Constructor of LogicalMLCommons.
   */
  public LogicalDrop(String tableName) {
    super(ImmutableList.of());
    this.tableName = tableName;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitDrop(this, context);
  }
}
