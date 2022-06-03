package org.opensearch.sql.libppl;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class LibPPL {

  public static void main(String[] args) {
    String[] queries =
        args.length > 0 ? args : new String[] {"source = stdin | stats count()", "source = stdin"};

    var sample = new ArrayList<Map<String, Object>>();
    sample.add(ImmutableMap.<String, Object>builder().put("testword", "abcd").put("testint", 1)
        .put("testbool", false).build());
    sample.add(ImmutableMap.<String, Object>builder().put("testword", "efgh").put("testint", 2)
        .put("testbool", true).build());
    LibPPLQueryAction queryAction = LibPPLQueryActionFactory.create(sample);

    // queryAction = LibPPLQueryActionFactory.createFromStdin();

    Arrays.stream(queries)
        .forEach(query -> {
          queryAction.execute(query);
        });
    queryAction.getOutput().forEach(map -> {
      map.forEach((key, value) -> {
        System.out.println(key + " : " + value);
      });
      System.out.println();
    });
  }
}
