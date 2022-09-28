package org.opensearch.sql.s3.client;

import org.opensearch.sql.catalog.model.CatalogMetadata;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

public class S3ClientImpl {
  private final S3Client client;

  public S3ClientImpl(CatalogMetadata catalogMetadata) {
    this.client = S3Client.create();
    // TODO create from auth
    // var auth = catalogMetadata.getAuthentication();
    // S3ClientBuilder builder = S3Client.builder();
    // AwsBasicCredentials
    //     cred = AwsBasicCredentials.create(configuration.getAccessKey(), configuration.getSecretKey());
  }
}
