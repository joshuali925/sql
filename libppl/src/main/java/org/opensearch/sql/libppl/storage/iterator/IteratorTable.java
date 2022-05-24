package org.opensearch.sql.libppl.storage.iterator;

import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.planner.DefaultImplementor;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.logical.LogicalRelation;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.storage.Table;

public class IteratorTable implements Table {
  private final Map<String, ExprType> fieldTypes;
  private final Iterator<ExprValue> iterator;

  public IteratorTable(Iterator<ExprValue> iterator) {
    this(ImmutableMap.<String, ExprType>builder().put("message", ExprCoreType.STRING).build(),
        iterator);
  }

  public IteratorTable(Map<String, ExprType> fieldTypes, Iterator<ExprValue> iterator) {
    this.fieldTypes = fieldTypes;
    this.iterator = iterator;
  }

  @Override
  public Map<String, ExprType> getFieldTypes() {
    return fieldTypes;
  }

  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    IteratorTableScan iteratorTableScan = new IteratorTableScan(iterator);
    return plan.accept(new iteratorImplementor(iteratorTableScan), iteratorTableScan);
  }

  @RequiredArgsConstructor
  public static class iteratorImplementor extends DefaultImplementor<IteratorTableScan> {
    private final IteratorTableScan indexScan;

    public PhysicalPlan visitRelation(LogicalRelation node, IteratorTableScan context) {
      return indexScan;
    }
  }
}
