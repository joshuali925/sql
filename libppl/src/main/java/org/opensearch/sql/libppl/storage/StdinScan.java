package org.opensearch.sql.libppl.storage;

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
import org.opensearch.sql.storage.TableScanOperator;

public class StdinScan extends TableScanOperator {
  private Iterator<ExprValue> iterator;

  public StdinScan() {
  }

  @Override
  public void open() {
    super.open();

    List<ExprValue> responses = new ArrayList<>();
    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String inputStr = "";
    while (inputStr != null) {
      try {
        inputStr = bufferedReader.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (inputStr != null) {
        LinkedHashMap<String, ExprValue> exprValueMap = new LinkedHashMap<>();
        exprValueMap.put("message", new ExprStringValue(inputStr));
        responses.add(new ExprTupleValue(exprValueMap));
      }
    }

    iterator = responses.iterator();
  }

  @Override
  public String explain() {
    throw new UnsupportedOperationException("Explain is not supported in LibPPL.");
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }
}
