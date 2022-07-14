package org.opensearch.sql.planner.logical;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.opensearch.sql.ast.expression.Literal;
import org.opensearch.sql.expression.Expression;

/**
 * ml-commons logical plan.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalCreateTable extends LogicalPlan {
  private final String tableName;

  private final Map<String, Literal> columns;

  /**
   * Constructor of LogicalMLCommons.
   */
  public LogicalCreateTable(String tableName, Map<String, Literal> columns) {
    // super(Collections.singletonList(child));
    super(ImmutableList.of());
    this.tableName = tableName;
    this.columns = columns;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitCreateTable(this, context);
  }
}
