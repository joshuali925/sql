package org.opensearch.sql.libppl.storage.iterator;

import java.util.Iterator;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.storage.TableScanOperator;

public class IteratorTableScan extends TableScanOperator {
  private Iterator<ExprValue> iterator;

  public IteratorTableScan(Iterator<ExprValue> iterator) {
    this.iterator = iterator;
  }

  @Override
  public void open() {
    super.open();
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
