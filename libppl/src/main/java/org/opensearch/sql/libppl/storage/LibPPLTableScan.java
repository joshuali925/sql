package org.opensearch.sql.libppl.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.storage.TableScanOperator;

public class LibPPLTableScan extends TableScanOperator {
  private Iterator<ExprValue> iterator;

  public LibPPLTableScan() {
    List<ExprValue> list = new ArrayList<ExprValue>();
    list.add(new ExprStringValue("a"));
    iterator = list.iterator();
  }

  @Override
  public String explain() {
    return null;
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
