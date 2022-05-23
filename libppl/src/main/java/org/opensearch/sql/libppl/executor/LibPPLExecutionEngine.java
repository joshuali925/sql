package org.opensearch.sql.libppl.executor;

import java.util.ArrayList;
import java.util.List;
import org.opensearch.sql.common.response.ResponseListener;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.planner.physical.PhysicalPlan;

public class LibPPLExecutionEngine implements ExecutionEngine {
  @Override
  public void execute(PhysicalPlan plan, ResponseListener<QueryResponse> listener) {
    List<ExprValue> result = new ArrayList<>();
    plan.open();
    while (plan.hasNext()) {
      result.add(plan.next());
    }
    listener.onResponse(new QueryResponse(plan.schema(), result));
  }

  @Override
  public void explain(PhysicalPlan plan, ResponseListener<ExplainResponse> listener) {

  }
}
