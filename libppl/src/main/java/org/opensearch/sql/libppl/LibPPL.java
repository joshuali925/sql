package org.opensearch.sql.libppl;

import java.util.Arrays;
import org.opensearch.sql.ppl.domain.PPLQueryRequest;

public class LibPPL {

  public static void main(String[] args) {
    LibPPLQueryAction queryAction = new LibPPLQueryAction();

    String[] queries = args.length > 0 ? args : new String[] {"source = stdin"};

    Arrays.stream(queries)
        .forEach(query -> queryAction.execute(new PPLQueryRequest(query, null, "")));
  }
}
