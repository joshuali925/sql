package org.opensearch.sql.libppl.storage.iterator;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.planner.DefaultImplementor;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.logical.LogicalRelation;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.storage.Table;

public class IterableTable implements Table {
  private final Map<String, ExprType> fieldTypes;
  private final Iterable<ExprValue> iterable;

  public IterableTable(Iterable<ExprValue> iterable) {
    this.iterable = iterable;
    Map<String, ExprType> fieldTypes = new LinkedHashMap<>();
    for (ExprValue exprValue : iterable) {
      Map<String, ExprValue> tuple = ExprValueUtils.getTupleValue(exprValue);
      tuple.keySet().forEach(field -> fieldTypes.put(field, tuple.get(field).type()));
    }
    this.fieldTypes = fieldTypes;
  }

  @Override
  public Map<String, ExprType> getFieldTypes() {
    return fieldTypes;
  }

  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    IterableTableScan iterableTableScan = new IterableTableScan(iterable.iterator());
    return plan.accept(new iteratorImplementor(iterableTableScan), iterableTableScan);
  }

  @RequiredArgsConstructor
  public static class iteratorImplementor extends DefaultImplementor<IterableTableScan> {
    private final IterableTableScan indexScan;

    public PhysicalPlan visitRelation(LogicalRelation node, IterableTableScan context) {
      return indexScan;
    }
  }
}
