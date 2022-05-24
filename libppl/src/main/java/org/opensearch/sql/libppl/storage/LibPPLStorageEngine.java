package org.opensearch.sql.libppl.storage;

import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.libppl.storage.iterator.IteratorTable;
import org.opensearch.sql.storage.StorageEngine;
import org.opensearch.sql.storage.Table;
import org.springframework.beans.factory.annotation.Autowired;

public class LibPPLStorageEngine implements StorageEngine {

  @Autowired
  private Iterable<ExprValue> input;

  @Override
  public Table getTable(String name) {
    // TODO: support other tables types
    // if (name.equals("stdin")) {
    //   return new StdinTable();
    // }

    return new IteratorTable(input.iterator());
  }
}
