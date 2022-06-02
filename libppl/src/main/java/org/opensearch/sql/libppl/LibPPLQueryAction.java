package org.opensearch.sql.libppl;

import static org.opensearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.common.response.ResponseListener;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.executor.ExecutionEngine;
import org.opensearch.sql.libppl.executor.LibPPLExecutionEngine;
import org.opensearch.sql.libppl.storage.LibPPLStorageEngine;
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
  private final Iterable<ExprValue> input;
  @Getter
  private Iterable<Map<String, Object>> output;

  public LibPPLQueryAction(Iterable<ExprValue> input) {
    this.input = input;
    pplService = getPPLService();
  }

  private PPLService getPPLService() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.registerBean(ExecutionEngine.class, () -> new LibPPLExecutionEngine());
    context.registerBean(StorageEngine.class, () -> new LibPPLStorageEngine(input));
    context.register(PPLServiceConfig.class);
    context.refresh();
    return context.getBean(PPLService.class);
  }

  public void execute(String pplQuery) {
    execute(new PPLQueryRequest(pplQuery, null, ""));
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
        List<Map<String, Object>> rows = new ArrayList<>();
        List<ExecutionEngine.Schema.Column> columns = result.getSchema().getColumns();
        for (Object[] objects : result) {
          TreeMap<String, Object> row = new TreeMap<>();
          for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i).getName(), objects[i]);
          }
          rows.add(row);
        }
        output = rows;
        // String formattedResult = formatter.format(result);
        // System.out.println(formattedResult);
      }

      @Override
      public void onFailure(Exception e) {
        log.error("Error happened during query handling", e);
      }
    };
  }

  public static class LibPPLCollectionsInput {
  }
}
