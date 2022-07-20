package org.opensearch.sql.planner.logical;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.opensearch.sql.ast.expression.RowFormatSerDe;

/**
 * ml-commons logical plan.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalDropTable extends LogicalPlan {
  private final String tableName;

  /**
   * Constructor of LogicalMLCommons.
   */
  public LogicalDropTable(String tableName) {
    super(ImmutableList.of());
    this.tableName = tableName;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitDropTable(this, context);
  }
}
