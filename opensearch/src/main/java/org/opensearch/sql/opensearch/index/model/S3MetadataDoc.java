package org.opensearch.sql.opensearch.index.model;

import java.util.Map;
import javax.annotation.Nullable;
import lombok.Data;
import org.opensearch.sql.ast.expression.RowFormatSerDe;

@Data
public class S3MetadataDoc {
  private final String tableName;
  private final Map<String, String> columns;
  private final RowFormatSerDe rowFormatSerDe;
  private final Map<String, String> rowFormatSerDeProperties;
  private final String partitionBy;
  private final String location;
}
