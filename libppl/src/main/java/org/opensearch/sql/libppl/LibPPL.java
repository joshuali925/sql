package org.opensearch.sql.libppl;

import org.opensearch.sql.ppl.domain.PPLQueryRequest;

public class LibPPL {

  public void parseArgs(String[] args) {
    // parse command line args
    
  }
  public static void main(String[] args) {
    LibPPLQueryAction queryAction = new LibPPLQueryAction();

    String query = args.length > 0 ? args[0] : "source = stdin";

    PPLQueryRequest request = new PPLQueryRequest(query, null, "");
    queryAction.execute(request);
  }
}
