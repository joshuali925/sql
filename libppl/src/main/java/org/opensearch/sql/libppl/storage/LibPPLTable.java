package org.opensearch.sql.libppl.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.planner.DefaultImplementor;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.logical.LogicalRelation;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.storage.Table;

public class LibPPLTable implements Table {
  @Override
  public Map<String, ExprType> getFieldTypes() {
    Map<String, ExprType> fieldTypes = new LinkedHashMap<>();
    fieldTypes.put("name", ExprCoreType.STRING);
    return fieldTypes;
  }

  @Override
  public PhysicalPlan implement(LogicalPlan plan) {
    return plan.accept(new DefaultImplementor<LibPPLTableScan>() {
      @Override
      public PhysicalPlan visitRelation(LogicalRelation node, LibPPLTableScan context) {
        return new LibPPLTableScan();
      }
    }, null);
  }
}
