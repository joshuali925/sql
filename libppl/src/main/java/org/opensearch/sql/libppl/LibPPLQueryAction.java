package org.opensearch.sql.libppl;

import static org.opensearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.common.response.ResponseListener;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.libppl.executor.LibPPLExecutionEngine;
import org.opensearch.sql.libppl.storage.LibPPLStorageEngine;
import org.opensearch.sql.libppl.storage.stdin.StdinHelper;
import org.opensearch.sql.ppl.PPLService;
import org.opensearch.sql.ppl.config.PPLServiceConfig;
import org.opensearch.sql.ppl.domain.PPLQueryRequest;
import org.opensearch.sql.protocol.response.QueryResult;
import org.opensearch.sql.protocol.response.format.CsvResponseFormatter;
import org.opensearch.sql.protocol.response.format.Format;
import org.opensearch.sql.protocol.response.format.RawResponseFormatter;
import org.opensearch.sql.protocol.response.format.ResponseFormatter;
import org.opensearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import org.opensearch.sql.protocol.response.format.VisualizationResponseFormatter;
import org.opensearch.sql.storage.StorageEngine;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LibPPLQueryAction {
  private final PPLService pplService;
  private static final Logger log = LogManager.getLogger(LibPPLQueryAction.class);

  public LibPPLQueryAction() {
    pplService = getPPLService();
  }

  private PPLService getPPLService() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.registerBean(StdinHelper.class);
    context.registerBean(ExecutionEngine.class, () -> new LibPPLExecutionEngine());
    context.registerBean(StorageEngine.class, () -> new LibPPLStorageEngine());
    context.register(PPLServiceConfig.class);
    context.refresh();
    return context.getBean(PPLService.class);
  }

  public void execute(PPLQueryRequest pplQueryRequest) {
    pplService.execute(pplQueryRequest, createListener(pplQueryRequest));
  }

  private ResponseListener<ExecutionEngine.QueryResponse> createListener(
      PPLQueryRequest pplRequest) {
    Format format = pplRequest.format();
    ResponseFormatter<QueryResult> formatter;
    if (format.equals(Format.CSV)) {
      formatter = new CsvResponseFormatter(pplRequest.sanitize());
    } else if (format.equals(Format.RAW)) {
      formatter = new RawResponseFormatter();
    } else if (format.equals(Format.VIZ)) {
      formatter = new VisualizationResponseFormatter(pplRequest.style());
    } else {
      formatter = new SimpleJsonResponseFormatter(PRETTY);
    }
    return new ResponseListener<ExecutionEngine.QueryResponse>() {
      @Override
      public void onResponse(ExecutionEngine.QueryResponse response) {
        QueryResult result = new QueryResult(response.getSchema(), response.getResults());
        String formattedResult = formatter.format(result);
        System.out.println(formattedResult);
      }

      @Override
      public void onFailure(Exception e) {
        log.error("Error happened during query handling", e);
      }
    };
  }
}
