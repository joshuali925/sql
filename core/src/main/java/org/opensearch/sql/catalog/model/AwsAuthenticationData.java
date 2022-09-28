/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.catalog.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwsAuthenticationData extends AbstractAuthenticationData {

  @JsonProperty(required = true)
  private String accessKeyId;

  @JsonProperty(required = true)
  private String secretAccessKey;

  @JsonProperty(required = false)
  private String sessionToken;
}
