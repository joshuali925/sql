/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.opensearch.s3;

import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.sql.data.model.ExprStringValue;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;

public class S3Scan implements Iterator<ExprValue> {

  private static final Logger log = LogManager.getLogger(S3Scan.class);

  private final Iterator<S3ObjectMetaData> s3Objects;

  private final S3ObjectContent content;

  public S3Scan(List<S3ObjectMetaData> s3Objects) {
    this.s3Objects = s3Objects.iterator();
    content = new S3ObjectContent();
  }

  public void open() {
    if (hasNext()) {
      final S3ObjectMetaData next = s3Objects.next();
      System.out.println("next file " + next);
      content.open(next);
    }
  }

  // either content not been consumed or s3 objects still not been consumed.
  @Override
  public boolean hasNext() {
    if (content.hasNext()) {
      return true;
    } else if (!s3Objects.hasNext()) {
      return false;
    } else {
      content.close();
      final S3ObjectMetaData next = s3Objects.next();
      System.out.println("next file " + next);
      content.open(next);
      return content.hasNext();
    }
  }

  @Override
  public ExprValue next() {
    return asExprValue(content.next());
  }

  private ExprValue asExprValue(String line) {
    return ExprTupleValue.fromExprValueMap(ImmutableMap.of("raw", new ExprStringValue(line)));
  }
}
