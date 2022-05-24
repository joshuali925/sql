package org.opensearch.sql.libppl.storage.stdin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StdinHelper {
  public List<ExprValue> input;

  public StdinHelper() {
    input = readStdin();
  }

  @Bean
  public Iterable<ExprValue> getInput() {
    return input;
  }

  // @Bean
  public Iterable<ExprValue> getFixedInput() {
    var list = new ArrayList<ExprValue>();
    list.add(new ExprStringValue("a"));
    return list;
  }

  public List<ExprValue> readStdin() {
    List<ExprValue> values = new ArrayList<>();
    try (InputStreamReader inputStreamReader = new InputStreamReader(System.in);
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      String line = "";
      while (line != null) {
        line = bufferedReader.readLine();
        if (line != null) {
          LinkedHashMap<String, ExprValue> exprValueMap = new LinkedHashMap<>();
          exprValueMap.put("message", new ExprStringValue(line));
          values.add(new ExprTupleValue(exprValueMap));
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read from stdin.", e);
    }

    return values;
  }
}
