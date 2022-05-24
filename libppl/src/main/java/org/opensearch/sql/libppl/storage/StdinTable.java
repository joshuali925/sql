package org.opensearch.sql.libppl.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.planner.DefaultImplementor;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.logical.LogicalRelation;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.storage.Table;

public class StdinTable implements Table {
  @Override
  public Map<String, ExprType> getFieldTypes() {
    Map<String, ExprType> fieldTypes = new LinkedHashMap<>();
    fieldTypes.put("message", ExprCoreType.STRING);
    return fieldTypes;
  }

  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    StdinScan stdinScan = new StdinScan();
    return plan.accept(new StdinImplementor(stdinScan), stdinScan);
  }

  @RequiredArgsConstructor
  public static class StdinImplementor extends DefaultImplementor<StdinScan> {
    private final StdinScan indexScan;

    public PhysicalPlan visitRelation(LogicalRelation node, StdinScan context) {
      return indexScan;
    }
  }
}
