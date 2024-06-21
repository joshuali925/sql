# Plugin Monitoring

**Table of contents**

::: {.contents local="" depth="1"}
:::

## Introduction

By a stats endpoint, you are able to collect metrics for the plugin
within the interval. Note that only node level statistics collecting is
implemented for now. In other words, you only get the metrics for the
node you\'re accessing. Cluster level statistics have yet to be
implemented.

## Node Stats

### Description

The meaning of fields in the response is as follows:

+---------------------+------------------------------------------------+
| Field name          | Description                                    |
+=====================+================================================+
| > request_total     | > Total count of request                       |
+---------------------+------------------------------------------------+
| > request_count     | > Total count of request within the interval   |
+---------------------+------------------------------------------------+
| default_c           | > Total count of simple cursor request         |
| ursor_request_total |                                                |
+---------------------+------------------------------------------------+
| default_c           | > Total count of simple cursor request within  |
| ursor_request_count | > the interval                                 |
+---------------------+------------------------------------------------+
| failed_r            | Count of failed request due to system error    |
| equest_count_syserr | within the interval                            |
+---------------------+------------------------------------------------+
| failed_r            | Count of failed request due to bad request     |
| equest_count_cuserr | within the interval                            |
+---------------------+------------------------------------------------+
| > fail              | Indicate if plugin is being circuit broken     |
| ed_request_count_cb | within the interval                            |
+---------------------+------------------------------------------------+

### Example

SQL query:

``` sh
>> curl -H 'Content-Type: application/json' -X GET localhost:9200/_plugins/_sql/stats
```

Result set:

``` sh
{
  "failed_request_count_cb" : 0,
  "default_cursor_request_count" : 10,
  "default_cursor_request_total" : 3,
  "failed_request_count_cuserr" : 0,
  "circuit_breaker" : 0,
  "request_total" : 70,
  "request_count" : 0,
  "failed_request_count_syserr" : 0
}
```
