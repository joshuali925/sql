/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.sql.s3.storage;

import lombok.RequiredArgsConstructor;
import org.opensearch.sql.s3.client.S3ClientImpl;
import org.opensearch.sql.storage.StorageEngine;
import org.opensearch.sql.storage.Table;

/**
 * S3 storage engine implementation.
 */
@RequiredArgsConstructor
public class S3StorageEngine implements StorageEngine {

  private final S3ClientImpl s3ClientImpl;

  @Override
  public Table getTable(String name) {
    return new S3Index(s3ClientImpl);
  }
}