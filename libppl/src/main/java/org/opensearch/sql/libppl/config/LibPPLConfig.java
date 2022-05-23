package org.opensearch.sql.libppl.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;

public class LibPPLConfig {
  public static Map<String, ExprType> getConfig() {
    Map<String, ExprType> fieldTypes = new LinkedHashMap<>();
    fieldTypes.put("name", ExprCoreType.STRING);
    return fieldTypes;
  }
}
