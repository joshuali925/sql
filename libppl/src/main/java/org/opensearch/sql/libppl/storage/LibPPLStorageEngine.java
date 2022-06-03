package org.opensearch.sql.libppl.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.model.ExprValueUtils;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.libppl.storage.iterator.IterableTable;
import org.opensearch.sql.storage.StorageEngine;
import org.opensearch.sql.storage.Table;

public class LibPPLStorageEngine implements StorageEngine {

  private final Iterable<ExprValue> input;

  public LibPPLStorageEngine(Iterable<ExprValue> input) {
    this.input = input;
  }

  @Override
  public Table getTable(String name) {
    // TODO: support other tables types
    // if (name.equals("stdin")) {
    //   return new StdinTable();
    // }

    return new IterableTable(input);
  }
}
