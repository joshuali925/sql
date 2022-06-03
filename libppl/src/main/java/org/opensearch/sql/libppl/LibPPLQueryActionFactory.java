package org.opensearch.sql.libppl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;

public class LibPPLQueryActionFactory {
  public static LibPPLQueryAction createFromStdin() {
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
    return new LibPPLQueryAction(values);
  }

  public static LibPPLQueryAction createFromString(Collection<String> input) {
    List<ExprValue> values = input.stream().map(line -> {
      LinkedHashMap<String, ExprValue> exprValueMap = new LinkedHashMap<>();
      exprValueMap.put("message", new ExprStringValue(line));
      return new ExprTupleValue(exprValueMap);
    }).collect(Collectors.toList());
    return new LibPPLQueryAction(values);
  }

  public static LibPPLQueryAction create(Collection<Map<String, Object>> input) {
    List<ExprValue> values = input.stream().map(map -> {
      LinkedHashMap<String, ExprValue> exprValueMap = new LinkedHashMap<>();
      map.forEach((key, value) -> {
        exprValueMap.put(key, ExprValueUtils.fromObjectValue(value));
      });
      return new ExprTupleValue(exprValueMap);
    }).collect(Collectors.toList());
    return new LibPPLQueryAction(values);
  }
}
