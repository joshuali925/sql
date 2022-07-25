package org.opensearch.sql.opensearch.index;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionFuture;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.create.CreateIndexResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.sql.opensearch.client.OpenSearchClient;

@RequiredArgsConstructor
public class SqlMetadataIndex {
  private static final Logger log = LogManager.getLogger(SqlMetadataIndex.class);
  protected static final String INDEX_NAME = ".opensearch-sql";
  private final OpenSearchClient client;

  public boolean createTable(String tableName, Map<String, Object> source) {
    createIndex();
    IndexRequest request = new IndexRequest(INDEX_NAME).source(source).create(true).id(tableName);
    ActionFuture<IndexResponse> indexResponseActionFuture = client.getNodeClient().index(request);
    IndexResponse response = indexResponseActionFuture.actionGet();
    return response.getResult() == DocWriteResponse.Result.CREATED;
  }

  public boolean dropTable(String tableName) {
    createIndex();
    DeleteRequest request = new DeleteRequest(INDEX_NAME, tableName);
    ActionFuture<DeleteResponse> deleteResponseActionFuture =
        client.getNodeClient().delete(request);
    DeleteResponse deleteResponse = deleteResponseActionFuture.actionGet();
    return deleteResponse.getResult() == DocWriteResponse.Result.DELETED;
  }

  private void createIndex() {
    if (!client.indexExists(INDEX_NAME)) {
      CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME).mapping(getIndexMapping());
      ActionFuture<CreateIndexResponse> actionFuture =
          client.getNodeClient().admin().indices().create(request);
      CreateIndexResponse response = actionFuture.actionGet();
      if (response.isAcknowledged()) {
        log.info("Acknowledged " + INDEX_NAME + " creation.");
      } else {
        throw new IllegalStateException("Failed to create index " + INDEX_NAME);
      }
    }
  }

  private XContentBuilder getIndexMapping() {
    XContentBuilder builder;
    try {
      builder = XContentFactory.jsonBuilder()
          .startObject()
          .field("dynamic", false)
          .startObject("properties")
          .startObject("name").field("type", "keyword").endObject()
          .endObject()
          .endObject();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create index mapping.", e);
    }
    return builder;
  }

}
