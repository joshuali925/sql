/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.opensearch.storage.s3;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Getter
public class S3ObjectMetaData {
  private final String bucket;
  private final String object;
}
