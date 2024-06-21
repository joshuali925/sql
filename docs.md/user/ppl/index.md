# OpenSearch PPL Reference Manual

## Overview

Piped Processing Language (PPL), powered by OpenSearch, enables
OpenSearch users with exploration and discovery of, and finding search
patterns in data stored in OpenSearch, using a set of commands delimited
by pipes (\|). These are essentially read-only requests to process data
and return results.

Currently, OpenSearch users can query data using either Query DSL or
SQL. Query DSL is powerful and fast. However, it has a steep learning
curve, and was not designed as a human interface to easily create ad hoc
queries and explore user data. SQL allows users to extract and analyze
data in OpenSearch in a declarative manner. OpenSearch now makes its
search and query engine robust by introducing Piped Processing Language
(PPL). It enables users to extract insights from OpenSearch with a
sequence of commands delimited by pipes () syntax. It enables
developers, DevOps engineers, support engineers, site reliability
engineers (SREs), and IT managers to effectively discover and explore
log, monitoring and observability data stored in OpenSearch.

We expand the capabilities of our Workbench, a comprehensive and
integrated visual query tool currently supporting only SQL, to run
on-demand PPL commands, and view and save results as text and JSON. We
also add a new interactive standalone command line tool, the PPL CLI, to
run on-demand PPL commands, and view and save results as text and JSON.

The query start with search command and then flowing a set of command
delimited by pipe ( for example, the following query retrieve firstname
and lastname from accounts if age large than 18.

``` 
source=accounts
| where age > 18
| fields firstname, lastname
```

-   **Interfaces**
    -   [Endpoint](interfaces/endpoint.md)
    -   [Protocol](interfaces/protocol.md)
-   **Administration**
    -   [Plugin Settings](admin/settings.md)
    -   [Security Settings](admin/security.md)
    -   [Monitoring](admin/monitoring.md)
    -   [Datasource Settings](admin/datasources.md)
    -   [Prometheus
        Connector](admin/connectors/prometheus_connector.md)
    -   [Cross-Cluster Search](admin/cross_cluster_search.md)
-   **Commands**
    -   [Syntax](cmd/syntax.md)
    -   [ad command](cmd/ad.md)
    -   [dedup command](cmd/dedup.md)
    -   [describe command](cmd/describe.md)
    -   [show datasources command](cmd/showdatasources.md)
    -   [eval command](cmd/eval.md)
    -   [fields command](cmd/fields.md)
    -   [grok command](cmd/grok.md)
    -   [kmeans command](cmd/kmeans.md)
    -   [ml command](cmd/ml.md)
    -   [parse command](cmd/parse.md)
    -   [patterns command](cmd/patterns.md)
    -   [rename command](cmd/rename.md)
    -   [search command](cmd/search.md)
    -   [sort command](cmd/sort.md)
    -   [stats command](cmd/stats.md)
    -   [where command](cmd/where.md)
    -   [head command](cmd/head.md)
    -   [rare command](cmd/rare.md)
    -   [top command](cmd/top.md)
    -   [metadata commands](cmd/information_schema.md)
-   **Functions**
    -   [Expressions](functions/expressions.md)
    -   [Math Functions](functions/math.md)
    -   [Date and Time Functions](functions/datetime.md)
    -   [String Functions](functions/string.md)
    -   [Condition Functions](functions/condition.md)
    -   [Relevance Functions](functions/relevance.md)
    -   [Type Conversion Functions](functions/conversion.md)
    -   [System Functions](functions/system.md)
-   **Optimization**
    -   [Optimization](../../user/optimization/optimization.md)
-   **Language Structure**
    -   [Identifiers](general/identifiers.md)
    -   [Data Types](general/datatypes.md)
-   **Limitations**
    -   [Limitations](limitations/limitations.md)
