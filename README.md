# dp-api-java
Java API Library for the Data Platform Core Services

## Overview
This library isolates clients from the Data Platform gRPC communications framework by providing Java language interfaces
for the Core Services.  Interaction with the Core Services is simplified and clients require little or no knowledge of the gRPC library and the Protocol Buffers RPC interfaces.

Provides basic connection and communications services to the following Data Platform services:
- Query Service 
- Ingestion Service 
- Annotations Service (current development)

Also contains utilities and resources for manipulating and processing data.

## API Library Build
The Java API Library source and resources are available from the online repository [dp-api-java](https://github.com/osprey-dcs/dp-api-common).  The library should be downloaded into an appropriate location within the client host platform.  The Java API Library is then built as a ##Maven## project with all build parameters in the `pom.xml` file within the installation directory.

The Maven utility is based upon the notation of software "lifecycle", which is reflected in its build commands.  The life stage commands that are appropriate here are the "package" and "install" commands.  

### Maven Package Build
Maven can be used to package the Java API library into Java Archive files (i.e., 'JARs') that can be used directly on the client platform.  The command is given by

`% mvn package`

executed in the library installation directory (i.e., where 'pom.xml' is located).  As instructed by the `pom.xml` file, the Maven package build creates two JARs
- `dp-api-java-1.x.x.jar`
- `dp-api-java-shaded-1.x.x.jar`

Note that the library version is included in the name suffix.  The build products are located in the standard target location of the Maven utility, which is given as

`jal_install/target`

where 'jal_install' is the installation directory of the Java API Library. The first JAR file contains all compiled byte code and resources from the library code base.  The second JAR is a "fat JAR" containing all library byte code plus all compiled dependencies required for library execution.  The fat JAR is likely the most useful; direct inclusion in the Java class path allows access to all library function.

### Maven Install
The Java API Library can be "installed" into the local client platform for use as a dependency within another Maven project.  This situation is much the same as requirement that the Data Platform [communications framework](https://github.com/osprey-dcs/dp-grpc) must be installed locally in the client as a dependency of this library.  The command is given by

`% mvn install`

executed in the library installation directory (i.e., where the library `pom.xml` file is located).  Once completed the Java API Library will be install locally on the client platform (typically in the `~/.m2` directory).  All library resources are then available to any Maven project by including the Java API Library Maven properties within as a dependency in the `pom.xml` file (i.e., populating the `<groupId>`, `<artifactId>`, and `<version>` XML attributes).    Specifically, the dependency is stipulated with the `pom.xml` inclusion

`<dependencies>`

    `...` 
    `<dependency>` 
    `<groupID>com.ospreydcs</groupId>` 
    `<artifactId>dp-api-java</artifactId>`
    `<version>1.x.x</version>`
    `</dependency>`
    `...`
    
`</dependencies>`


## Library Configuration
The Java API Library requires the configuration file `dp-api-config.yml` which contains all the default configuration parameters for the library.  Most parameters are set to their optimal values but users are able to tune parameters
for optimal performance on the host platform.  However, it is necessary that the address of the Data Platform Core Service **must** be correctly specified in the configuration file.  These addresses (the URL and port) are found in the `connections` section of the configuration file.

### Static Configuration
There are a default configuration files in the `..main/resources` directories of the project which are built into the Java archive products.  Any configuration parameters within the `/resources` directories should be set **before** the library build - as the result is built directly into any Java API Library build products.  If no external configuration resources are found (see 'Dynamic Configuration' below), the library then refers to the configuration within its local resource section.  Clearly this is not optimal and dynamic configuration is generally preferred.

### Dynamic Configuration
The Java API Library configuration can be established "on the fly," specifically, at the time of the library start up.  Thus, any modification to the `dp-api-config.yml` file will be effective post build.  However, once the Java API Library has been started all library configurations are loaded at the time of first access, and any modifications will not be noticed until the library has shut down and re-started.

#### <a id="dp_api_java_home"> The `DP_JAL_HOME` Environment Variable </a>
The dynamic library configuration described above is facilitated using the environment variable `DP_JAL_HOME`. The Java API Library installation directory contains the sub-directory `config` which will hold all up-to-date configuration files.  Any configuration required by the library will then refer to file in that directory.  In order that this mechanism function correctly the `DP_JAL_HOME` environment variable must be set to the location of the Java API Library install, say `jal_install`.  This is most easily accomplished by modifying the `~/.dp.env` Data Platform environment file to include the additional line

`export DP_JAL_HOME=jal_install`,

where again `jal_install` is the location of the local Java API Library installation.  (Recall that the primary function for file `~/.dp.env` is to set the environment variable `DP_HOME` to the location of the Data Platform core services installation.)  Of course setting the environment variable `DP_JAL_HOME` to the proper location any time before invoking the Java API Library will also allow dynamic library configuration.

#### Java Virtual Machine Arguments
The Java Virtual Machine (VM) allows command-line arguments starting with the `-D` option which appear as "system properties" within the Java VM.  The Java API Library will also check the Java VM system property `DP_JAL_HOME` to see if it has been set to the installation location `jal_install`.  Thus, the `DP_JAL_HOME` is also a Java VM property that can be set when starting a Java application from the command line (i.e., one that uses the JAVA API Library).  For example say an application `my_app` uses the Java API Library, the follow command sets the Java VM system property to the proper value for dynamic library configuration:

`%java -cp dp-api-java-shaded-1.x.x.jar -DDP_JAL_HOME=jal_install my_app`

To conform to the Java standard for system properties, the property `dp.jal.home` is also a valid identifier for the Java API Library installation location (i.e., in addition to `DP_JAL_HOME`).  For example, for an executable Java archive (JAR) `my-exec-jar.jar` with main class `org.package.my-app` the following command sets the Java VM system property for dynamic library configuration

`%java -jar my-exec-jar.jar -cp dp-api-java-shaded-1.x.x.jar -Ddp.api.java.home=jal_install org.package.my_app`

## Connection Factories
Connection to the Data Platform Core Services are facilitated using "connection factories."  Typically these are static classes (instance classes are also available but not recommended) that provide a collection of methods for connecting to a Data Platform service and returning a Java interface to the service.  The methods contain argument signatures that specify various connection parameters, including the default connection which contains no arguments.  Note that the default connection is specified completely in the configuration file `dp-api-config.yml`. See the class documentation for each connection factory for additional information.

### Interface Shut Down
All interfaces are returned by the connection factories connected to the intended Data Platform Core Service and are ready for use.  Whenever an interface is no longer needed it should be shut down using a `shutdown()` operation.  The `shutdown()` operation releases all gRPC resources used by the interface, which is necessary to maintain performance.

## Ingestion Service API
The client Ingestion Service API resources are available in the package `com.ospreydcs.dp.api.ingest`.  The connection factory 
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

## Query Service API
The client Query Service API resources are available in the package `com.ospreydcs.dp.api.query`.  The connection factory
for obtaining Query Service interfaces is the class `DpQueryApiFactory`.  There also some Query API resources within
the `com.ospreydcs.dp.api.model` package (e.g., `IDataTable`, `PvMetadataRecord`, etc.).

### Query Service API
The Query Service API for the Java API Library contains a only single interface `IQueryService`.  The interface contains operations for both standard unary RPC time-series data requests (blocking, synchronous requests) and streaming time-series data requests (non-blocking, asynchronous requests).  The interface also contains operations supporting the available metadata requests.

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

## Annotation Service API
The client Annotation Service API resources are available in the package `com.ospreydcs.dp.api.annotation`.  The connection factory for obtaining Query Service interfaces is the class `DpAnnotationServiceApiFactory`.  There also some Query API resources within the `com.ospreydcs.dp.api.model` package (e.g., `OwnerUID`, `DpDataset`, etc.).

### Annotation Service API
The Annotation Service API for the Java API Library currently contains a the single interface `IAnnotationService`.  The interface contains operations for both data set management and annotation management.  Data sets and annotations are described in further detail below.

### Data Sets
Data sets are the fundamental mechanism for performing archive annotations.  They identify regions of the time-series data archive to which annotations are attached.  Thus, in order to create an archive annotation one must first create the data set that is being annotated.  This is accomplished with a 'create data set request' using an `DpCreateDatasetRequest` instance.  The request operation returns a unique identifier (UID) for the data set which is generated by the Annotation Service.  The data set UID is used to identify the data set for all annotations subsequently created.

### Annotations
There are 3 supported annotation types:
1. Comments - simple text descriptions of a data set within the time-series archive
2. Associations - relationships between two or more data sets within the time-series archive (typically with a description)
3. Calculations - stored post-ingestion calculations obtained from time-series data within the archive

Annotations are attached to regions of the time-series data archive defined by 'data sets' (see above).  Data Platform clients create annotations within the data archive to provide a "value-added" data processing mechanism available to all other clients.  For example, a Data Platform client may identify a correlation between two data sets; the client can then create an association between the two data sets which is then retrievable by other clients for further processing (e.g., creating calculations based upon the correlation).

## Java API Library Tools
The Java API Library ships with a set of tools and utilities.  These are executables used for performance and evaluations measurements, as well as testing the operation of individual API Library components.  The tools are implemented as "main classes" within the Java API Library and can be executed directly from the `dp-api-java-shaded-1.x.x.jar`.  However, the library ships with a collection of shell scripts to ease tool execution.  The shell scripts are located in directory

`jal_install/bin`

where `jal_install` is the location of the Java API Library installation.  

### Tools Configuration
The environment variable `DP_JAL_HOME` must be set to the location of the local installation (i.e., "`jal_install`") to ensure that the above scripts run correctly. Recall that modifying the Data Platform environment file `~/.dp.env` is the straightforward method for setting this variable; see the section (The `DP_JAL_HOME` Environment Variable)[#dp_api_home].    

Additionally, there are other environment variables that should be set for some tools to operate correctly.  Many Java API Library tools require an output location.  Specifically, they create reports summarizing their results and require an output file to deposit these reports.  The environment variable `DP_API_JAVA_TOOLS_OUTPUT` should be set to the output location on the local platform for all Java API Library tools.  

### Tools Output Locations
The directories for Java API Library tools output must be created in advance.  Thus, the following line should be included in the `~/.dp.env` file:

`export DP_JAL_OUTPUT=my-local-output-directory`

where `my-local-output-directory` is the location for tools outputs.  As with the `DP_JAL_HOME` variable, the `DP_JAL_OUTPUT` can be included in the Java command-line, where it also has the synonym `dp.jal.output`.

Typically each tools has a separate sub-directory structure where it deposits its results.  For example, the `QueryChannelEvalutor` application, which can be started with script `app-run-querychannel-evaluator` has the sub-directory `query/channel` as its output location.  Thus, the directory

`my-local-output-directory/query/channel`

must exist on the location platform.

### Execution
Java API Library tools execution is performed using the usual (bash) shell script execution for the local platform.  For example, to execute the `QueryChannelEvaluator` tool execute the following from the `jal_install/bin` directory:

`%./app-run-querychannel-evaluator`

