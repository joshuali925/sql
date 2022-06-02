package org.opensearch.sql.libppl;

import java.util.ArrayList;
import java.util.Arrays;

public class LibPPL {

  public static void main(String[] args) {
    String[] queries = args.length > 0 ? args : new String[] {"source = stdin | parse message '(?<digit>\\d).*' | eval n =  cast(digit as int)"};

    var sample = new ArrayList<String>();
    sample.add("1abc");
    sample.add("2def");
    sample.add("3ghi");
    LibPPLQueryAction queryAction = LibPPLQueryActionFactory.createFromString(sample);

    Arrays.stream(queries)
        .forEach(query -> {
          queryAction.execute(query);
          queryAction.getOutput();
          // queryAction.getOutput().forEach(o -> {
          //   System.out.println(o.getClass().getName());
          //   System.out.println(Arrays.toString(o));
          //   for (Object value : o) {
          //     System.out.println(value.getClass().getName());
          //     System.out.println(value);
          //   }
          // });
        });
  }
}
