package org.opensearch.sql.planner.logical;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.opensearch.sql.ast.expression.Literal;
import org.opensearch.sql.ast.expression.RowFormatSerDe;

/**
 * ml-commons logical plan.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalCreateTable extends LogicalPlan {
  private final String tableName;

  private final Map<String, String> columns;
  private final RowFormatSerDe rowFormatSerDe;
  private final Map<String, String> rowFormatSerDeProperties;
  private final String partitionBy;
  private final String location;

  /**
   * Constructor of LogicalMLCommons.
   */
  public LogicalCreateTable(String tableName, Map<String, String> columns,
                            RowFormatSerDe rowFormatSerDe,
                            Map<String, String> rowFormatSerDeProperties, String partitionBy,
                            String location) {
    super(ImmutableList.of());
    this.tableName = tableName;
    this.columns = columns;
    this.rowFormatSerDe = rowFormatSerDe;
    this.rowFormatSerDeProperties = rowFormatSerDeProperties;
    this.partitionBy = partitionBy;
    this.location = location;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitCreateTable(this, context);
  }
}
