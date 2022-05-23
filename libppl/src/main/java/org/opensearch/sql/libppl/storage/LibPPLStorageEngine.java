package org.opensearch.sql.libppl.storage;

import org.opensearch.sql.storage.StorageEngine;
import org.opensearch.sql.storage.Table;

public class LibPPLStorageEngine implements StorageEngine {
  @Override
  public Table getTable(String name) {
    return new LibPPLTable();
  }
}
