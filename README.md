# dp-api-common
Java API Library for the Data Platform Core Services

## Overview
This library isolates clients from the Data Platform gRPC communications framework by providing Java language interfaces
for the Core Services.  Interaction with the Core Services is simplified and clients require little or no knowledge of 
the gRPC library and the Protocol Buffers RPC interfaces.

Provides basic connection and communications services to the following Data Platform services:
- Query Service 
- Ingestion Service 
- Annotations Service (future development)

Also contains utilities and resources for manipulating and processing data.

## Library Configuration
The Java API Library requires the configuration file `dp-api-config.yml` which contains all the default configuration
parameters for the library.  Most parameters are set to their optimal values but users are able to tune parameters
for optimal performance on the host platform.  However, it is necessary that the address of the Data Platform 
Core Service **must** be correctly specified in the configuration file.  These addresses (the URL and port) are 
found in the `connections` section of the configuration file.

## Connection Factories
Connection to the Data Platform Core Services are facilitated using "connection factories."  Typically these are
static classes (instance classes are also available but not recommended) that provide a collection of methods
for connecting to a Data Platform service and returning a Java interface to the service.  The methods contain
argument signatures that specify various connection parameters, including the default connection which contains
no arguments.  Note that the default connection is specified completely in the configuration file `dp-api-config.yml`.
See the class documentation for each connection factory for additional information.

### Interface Shut Down
All interfaces are returned by the connection factories connected to the intended Data Platform Core Service and
are ready for use.  Whenever an interface is no longer needed it should be shut down using a `shutdown()`
operation.  The `shutdown()` operation releases all gRPC resources used by the interface, which is necessary to
maintain performance.

## Ingestion API
The client ingestion API resources are available in the package `com.ospreydcs.dp.api.ingest`.  The connection factory 
for obtaining all Ingestion Service interfaces is the class `DpIngestionApiFactory`.

### Ingestion Frames
All data ingestion is performed using `IngestionFrame` instances.  Specifically, all ingestion APIs accept only 
`IngestionFrame` instances.  Instances of this class are essentially tables of heterogeneous, correlated, sampled data
with either an explicit list of timestamps or a uniform sampling clock specifying sampling rates.  `IngestionFrame`
instances may also contain additional attributes to provide additional context for the ingested data which may
be queried against (e.g., experiment name, facility location, machine hardware, etc.)

### Ingestion APIs
The Ingestion API for the Java API Library contains the following 2 separate interfaces:
- `IIngestionService` - provides standard RPC unary ingestion where ingestion is blocking and synchronized.
- `IIngestionStream` - provides streaming ingestion where ingestion is asynchronous over gRPC data streams.

The `IIngestionService` interface is to be used for small ingestion frames under light loading.  Ingestion frame
sizes are limited by the gRPC message size limit, any frame with memory allocation larger than this limit will
be rejected.  Note that gRPC message size limit can be changed within the library configuration file `dp-api-config.yml`
in the `connections` section.  However, message size limits must still be less than that accepted by the Data
Platform Core Service.  Additionally, increasing gRPC message size limits will eventually lead to performance degradation.
If large data frames are necessary clients should use the `IIngestionStream` interface, it has a utility for decomposing
large frames into multiple composite frames meeting gRPC message size limits.  See the class documentation for the
`IIngestionService` interface for additional information.

The `IIngestionStream` interface is intended for fast ingestion and/or heavy loading.  The interface maintains an open
gRPC data stream with the Ingestion Service and `IngestionFrame` instances can be supplied as they arrive, data transport
is done asynchronously.  The interface implementations maintain a message buffer for staging ingestion data for transport,
this buffer isolates the Ingestion Service from any ingestion spikes.  It can also be configured for finite capacity
(the default configuration) so clients feel 'back pressure', that is, ingestion is throttled and the interface refuses
to accept any ingestion frames until the buffer is ready (i.e., the interface blocks).  

There are no ingestion frame allocation limits for the `IIngestionStream` interface.  The interface has a component for
decomposing large ingestion frames into smaller composite frames whenever a frame is larger than the limit specified in
the configuration file.  Note that it is also possible to disable ingestion frame decomposition if it is known that
ingestion frames will always have allocation less than the limit; this option is desirable since it will increase
ingestion performance.

## Query API
The client ingestion API resources are available in the package `com.ospreydcs.dp.api.query`.  The connection factory
for obtaining Query Service interfaces is the class `DpQueryApiFactory`.

### Query API
The Query API for the Java API Library contains a only single interface `IQueryService`.  The interface contains operations
for both standard unary RPC time-series data requests (blocking, synchronous requests) and streaming time-series data
requests (non-blocking, asynchronous requests).  The interface also contains operations supporting the available metadata requests.

### Time-series Data Requests
Time-series data requests are created with the `DpDataRequest` class.  This class is essentially a 'request builder' utility offering multiple `select...()`, `range...()`, and `filter...()` methods for selecting data sources (process variable names), specifying time ranges, and filtering on data source values, respectively.  Once clients have created the desired query
request the `DpDataRequest` instance is presented to the Query Service interface for result set recovery.  Note that not
all features offered by `DpDataRequest` are available yet by the Query Service.

The result sets of time-series data requests are returned as instances of the `IDataTable` interface.  This presents a
table-like interface to the heterogeneous data where correlated data samples are contained in table columns recoverable
by data source name.  The table also contains a separate column of timestamps applicable to all column data.

Time-series data requests can be performed using unary RPC operations and streaming gRPC operations (although clients
are isolated from the details of gRPC).  In the unary case result sets must be limited to the maximum gRPC message size.
For the streaming situation there are no request size limits, in fact the entire archive can be queried for.  In the 
streaming case request data is returned in raw format (i.e., using 'data buckets') and correlated on the host platform.
This situation offloads processing requiring from the Query Service increase overall performance.  Note that there are
configuration parameters for tuning the request data reconstruction in the Java API Library configuration file
`dp-api-config.yml`.

See the class documentation for `IQueryService` interface for further details. 

### Metadata Requests
The Data Platform archive contains metadata associated with the time-series data.  This includes snapshot IDs, Data Provider
names and IDs, and process variable attributes.  Currently, the only metadata available through the Query Service is the
available process variable names within the archive, and the time ranges for these process variables.

Metadata requests are created with the `DpMetadataRequest` class, which is a request builder for metadata.  Currently, the
class offers methods for querying for process variable metadata.  The result sets of these metadata requests are 
returned as collections of metadata records represented by `PvMetadataRecord`.  There is a single record for every process
variable matching the request.

All metadata requests utilize unary gRPC operations.  See the class documentation for `IQueryService` interface for further details.
