package org.opensearch.sql.libppl;

import static org.opensearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;

import org.opensearch.sql.common.response.ResponseListener;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.libppl.executor.LibPPLExecutionEngine;
import org.opensearch.sql.libppl.storage.LibPPLStorageEngine;
import org.opensearch.sql.ppl.PPLService;
import org.opensearch.sql.ppl.config.PPLServiceConfig;
import org.opensearch.sql.ppl.domain.PPLQueryRequest;
import org.opensearch.sql.protocol.response.QueryResult;
import org.opensearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import org.opensearch.sql.storage.StorageEngine;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LibPPL {
  private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
  private PPLService pplService;

  public LibPPL() {
    context.registerBean(ExecutionEngine.class, () -> new LibPPLExecutionEngine());
    context.registerBean(StorageEngine.class, () -> new LibPPLStorageEngine());
    context.register(PPLServiceConfig.class);
    context.refresh();
    pplService = context.getBean(PPLService.class);
  }

  public static void main(String[] args) {
    LibPPL libPPL = new LibPPL();
    String query = args.length > 0 ? args[0] : "source=dummy";
    PPLQueryRequest request = new PPLQueryRequest(query, null, "");
    libPPL.pplService.execute(request,
        new ResponseListener<ExecutionEngine.QueryResponse>() {
          @Override
          public void onResponse(ExecutionEngine.QueryResponse response) {
            QueryResult result = new QueryResult(response.getSchema(), response.getResults());
            String json = new SimpleJsonResponseFormatter(PRETTY).format(result);
            System.out.println(json);
          }

          @Override
          public void onFailure(Exception e) {
            throw new IllegalStateException("Exception happened during execution", e);
          }
        });
  }
}
