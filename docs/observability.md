# Observability

Observability in Spring Boot centers on the use of the Spring Boot Actuator, which provides a set of production-ready
features to help you monitor and manage your application.

## Spring Boot Actuator

The Spring Boot Actuator is a powerful tool that exposes various endpoints to monitor and manage your application. It
provides insights into the application's health, metrics, environment, and more. To enable the Actuator, you need to add
the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

After adding this dependency, you can access the Actuator endpoints at `http://localhost:8080/actuator`. By default, you
won't see very much listed here because only a few endpoints are exposed by default. You can expose all endpoints by
modifying your `application.properties` file:

```properties
management.endpoints.web.exposure.include=*
```

Once you have this configuration, you can access various endpoints, including:

- `/actuator/health`: Provides health status of the application.
- `/actuator/info`: Displays application information such as version and build details.
- `/actuator/sbom`: Displays the Software Bill of Materials (SBOM) for the application.
- `/actuator/configprops`: Lists all configuration properties of the application (masked by default).
- `/actuator/env`: Displays environment properties (masked by default).
- `/actuator/loggers`: Allows you to view and modify the logging levels of your application.
- `/actuator/threaddump`: Provides a thread dump of the application.
- `/actuator/liquibase`: Displays Liquibase database migration status.
- `/actuator/mappings`: Lists all the request mappings in your application.

Let's take a closer look at some of these endpoints:

### Health Endpoint

The `/actuator/health` endpoint provides a summary of the application's health status. It checks various components such
as database connectivity, disk space, and other critical services. You can access it at
`http://localhost:8080/actuator/health`. By default, it returns a simple JSON response indicating the overall health
status:

```json
{
  "status": "UP"
}
```

To view more detailed health information, you can set the `management.endpoint.health.show-details` property to `always`
in your `application.properties` file:

```properties
management.endpoint.health.show-details=always
```

Now you should see a more detailed response when you access the health endpoint. As you add dependencies for other
technologies and frameworks, Actuator will automatically add health information for them if available. It is also
possible to configure your own health indicators, but that is beyond the scope of this workshop. The default indicators
cover most common scenarios.

### Info Endpoint

The `/actuator/info` endpoint provides metadata about your application. You can access it at
`http://localhost:8080/actuator/info`. By default, it returns an empty JSON object:

```json
{}
```

Let's fix that! To populate this endpoint with useful information, modify our `pom.xml` file:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>build-info</id>
                    <goals>
                        <goal>build-info</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

After adding this configuration, rebuild your application using Maven:

```bash
mvn clean install
```

Now, when you access the `/actuator/info` endpoint, you should see information about your application, including the
version, build time, and other metadata:

```json
{
  "build": {
    "artifact": "spring-boot-workshop",
    "name": "Spring Boot Workshop",
    "time": "2025-06-08T14:01:36.831Z",
    "version": "0.0.1-SNAPSHOT",
    "group": "com.callibrity.spring"
  }
}
```

We can add Git information to this endpoint as well. To do this, add the following plugin to your `pom.xml`:

```xml

<plugin>
    <groupId>io.github.git-commit-id</groupId>
    <artifactId>git-commit-id-maven-plugin</artifactId>
</plugin>
```

After adding this plugin, rebuild your application again and check the `/actuator/info` endpoint. You should now see Git
information:

```json
{
  "git": {
    "branch": "main",
    "commit": {
      "id": "26bfe19",
      "time": "2025-06-06T02:27:32Z"
    }
  },
  "build": {
    "artifact": "spring-boot-workshop",
    "name": "Spring Boot Workshop",
    "time": "2025-06-08T14:06:24.313Z",
    "version": "0.0.1-SNAPSHOT",
    "group": "com.callibrity.spring"
  }
}
```

If you'd like to see more detailed Git information, you can configure the endpoint in your `application.properties` file:

```properties
management.info.git.mode=full
```

### SBOM Endpoint

The `/actuator/sbom` endpoint provides a Software Bill of Materials (SBOM) for your application. An SBOM is a list of
all the components, libraries, and dependencies used in your application, along with their versions. This is useful for
security and compliance purposes. To enable the SBOM endpoint, we need to generate the SBOM information during the build
process. To do this, we need another plugin in our `pom.xml`:

```xml
<plugin>
    <groupId>org.cyclonedx</groupId>
    <artifactId>cyclonedx-maven-plugin</artifactId>
</plugin>
```

After adding this plugin, rebuild your application and check the `/actuator/sbom/application` endpoint. You should see a JSON representation of the SBOM:

```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.6",
  "version": 1
}
```

## Logging in Spring Boot
Logging is a crucial aspect of observability in any application. Spring Boot provides [Logback](https://logback.qos.ch/) as the default logging framework, which is highly configurable and supports various logging formats. By default, Spring Boot logs messages to the console in a human-readable format.

Your code should use the [SLF4J](https://www.slf4j.org/) API for logging, which is a simple facade for various logging frameworks (including Logback). This allows you to switch between different logging implementations without changing your code. Let's add some logging to our `DefaultPersonService`:

```java
package com.callibrity.spring.workshop.app;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultPersonService implements PersonService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPersonService.class);
    
    private final PersonRepository repository;

    public DefaultPersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PersonDto createPerson(String firstName, String lastName) {
        log.info("Creating person with first name {} and last name {}", firstName, lastName);
        var person = new Person(firstName, lastName);
        return mapToDto(repository.save(person));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDto retrievePersonById(String id) {
        log.info("Retrieving person with id {}", id);
        return mapToDto(findById(id));
    }
    // Other methods omitted for brevity
}
```

Let's take a closer look at how we added logging to our service:
- We created a static final logger instance using `LoggerFactory.getLogger()`, passing the class object as an argument.
- We added logging statements in the `createPerson` and `retrievePersonById` methods using the `log.info()` method. This will log informational messages that can help us understand the flow of the application.
- We used parameterized logging to avoid unnecessary string concatenation when the log level is not enabled. This is a best practice that improves performance!

## Metrics in Spring Boot

Metrics are another important aspect of observability. Spring Boot Actuator provides built-in support for collecting and exposing metrics about your application. Out-of-the-box, Spring Boot provides a comprehensive set of metrics that can be used to monitor the performance and health of your application. These metrics include information about HTTP requests, database connections, cache usage, and more. Take a look at the list of available metrics by accessing the `/actuator/metrics` endpoint at `http://localhost:8080/actuator/metrics`. You will see a list of available metrics, such as `jvm.memory.used`, `http.server.requests`, and many others:

```json
{
  "names": [
    "application.ready.time",
    "application.started.time",
    "disk.free",
    "disk.total",
    "hikaricp.connections",
    "hikaricp.connections.acquire",
    "hikaricp.connections.active",
    "hikaricp.connections.creation",
    "hikaricp.connections.idle",
    "hikaricp.connections.max",
    "hikaricp.connections.min",
    "hikaricp.connections.pending",
    "hikaricp.connections.timeout",
    "hikaricp.connections.usage",
    "http.server.requests",
    "http.server.requests.active",
    "jdbc.connections.active",
    "jdbc.connections.idle",
    "jdbc.connections.max",
    "jdbc.connections.min",
    "jvm.buffer.count",
    "jvm.buffer.memory.used",
    "jvm.buffer.total.capacity",
    "jvm.classes.loaded",
    "jvm.classes.unloaded",
    "jvm.compilation.time",
    "jvm.gc.live.data.size",
    "jvm.gc.max.data.size",
    "jvm.gc.memory.allocated",
    "jvm.gc.memory.promoted",
    "jvm.gc.overhead",
    "jvm.info",
    "jvm.memory.committed",
    "jvm.memory.max",
    "jvm.memory.usage.after.gc",
    "jvm.memory.used",
    "jvm.threads.daemon",
    "jvm.threads.live",
    "jvm.threads.peak",
    "jvm.threads.started",
    "jvm.threads.states",
    "logback.events",
    "process.cpu.time",
    "process.cpu.usage",
    "process.files.max",
    "process.files.open",
    "process.start.time",
    "process.uptime",
    "service.person",
    "service.person.active",
    "spring.data.repository.invocations",
    "system.cpu.count",
    "system.cpu.usage",
    "system.load.average.1m",
    "tomcat.sessions.active.current",
    "tomcat.sessions.active.max",
    "tomcat.sessions.alive.max",
    "tomcat.sessions.created",
    "tomcat.sessions.expired",
    "tomcat.sessions.rejected"
  ]
}
```

To record your own metrics, you will use [Micrometer](https://micrometer.io/), which is the metrics facade used by Spring Boot. First, we'll need to add a dependency:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <scope>runtime</scope>
</dependency>
```

Micrometer provides a simple API to record custom metrics, such as counters, gauges, and timers. You can use these metrics to track application performance, user interactions, and other important events. Typically, you will use Micrometer's `@Observable` annotation to mark classes and methods that you want to track:

```java
@Service
@Observed(name="service.person")
public class DefaultPersonService implements PersonService {
    // Code omitted for brevity
}
```

The standard naming convention for Micrometer metrics is dot-separated, similar to package names. In this case, we named the metric `service.person`, which indicates that it is related to the `PersonService`. Since Micrometer is a facade, it will automatically adapt the metric names (if you stick to the convention) to be appropriate for the underlying monitoring system you are using, such as Prometheus, InfluxDB, or others. Let's take a look at the metrics that are recorded by Micrometer when you use the `DefaultPersonService` by accessing the `/actuator/metrics/service.person` endpoint:

```json
{
  "name": "service.person",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 14.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 0.06342246
    },
    {
      "statistic": "MAX",
      "value": 0.0
    }
  ],
  "availableTags": [
    {
      "tag": "method",
      "values": [
        "createPerson",
        "retrievePersonById"
      ]
    },
    {
      "tag": "error",
      "values": [
        "none"
      ]
    },
    {
      "tag": "class",
      "values": [
        "com.callibrity.spring.workshop.app.DefaultPersonService"
      ]
    }
  ]
}
```

## Tracing in Spring Boot

Tracing is another important aspect of observability, especially in distributed systems. It allows you to track the flow of requests through your application and understand how different components interact with each other. Again, we will look to Micrometer for this functionality:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing</artifactId>
</dependency>
```

Since we're already using Micrometer's `@Observed` annotation, we get tracing for free! Micrometer will automatically create spans for the methods (and classes) annotated with `@Observed`. There's no good built-in way to visualize the traces, so we'll have to set that up. Let's do that now.

## Using OpenTelemetry

[OpenTelemetry](https://opentelemetry.io/) is a set of APIs, libraries, agents, and instrumentation to provide observability for applications. It supports distributed tracing and metrics collection, making it a powerful tool for monitoring and debugging applications. To get started using Open Telemetry, we will first import the Open Telemetry Instrumentation BOM (Bill of Materials) by adding the following to our `pom.xml` (right below the `<dependencies>` section):

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.opentelemetry.instrumentation</groupId>
            <artifactId>opentelemetry-instrumentation-bom</artifactId>
            <version>${opentelemetry-instrumentation.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Let's be sure to define the `opentelemetry-instrumentation.version` property in our `pom.xml`:

```xml
<properties>
    <opentelemetry-instrumentation.version>2.16.0</opentelemetry-instrumentation.version>
</properties>
```

This will ensure that all the OpenTelemetry instrumentation dependencies are managed and can be easily updated. Next, we will add the OpenTelemetry Spring Boot Starter dependency:

```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
</dependency>
```

_Note: Since we imported the OpenTelemetry Instrumentation BOM, we don't need to specify a version for this dependency!_

We will also need to add the OpenTelemetry Bridge for Micrometer to enable tracing:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
```
_Note: The abbreviation "otel" stands for OpenTelemetry._

We will also need to tell OpenTelemetry to enable the Micrometer instrumentation. We can do this by adding the following property to our `application.properties` file:

```properties
otel.instrumentation.micrometer.enabled=true
```

By default, Spring Boot will "sample" 10% of the traces, meaning that only 10% of the requests will be traced. This is a good starting point for development and testing, but you can adjust the sampling rate in your `application.properties` file:

```properties
management.tracing.sampling.probability=1.0
```

This will cause 100% of the requests to be traced, which is useful for development and debugging. In production, you might want to lower this value to reduce the overhead of tracing.

We will also want to tell OpenTelemetry a little bit about our application, so that we can identify it in traces and filter results. We can do this by adding the following properties to our `application.properties` file:

```properties
otel.resource.attributes.service.name=${spring.application.name}
otel.resource.attributes.service.namespace=callibrity
otel.resource.attributes.deployment.environment=workshop
```
Now that we have the necessary dependencies in place, we can restart our application.

### Gotcha!

If you wait a bit, you should see some output in the console that looks like this:

```text
2025-06-08T13:45:12.810-04:00 ERROR 21527 --- [Spring Boot Workshop] [alhost:4318/...] [                                                 ] i.o.exporter.internal.http.HttpExporter  : Failed to export logs. The request could not be executed. Full error message: Failed to connect to localhost/[0:0:0:0:0:0:0:1]:4318

java.net.ConnectException: Failed to connect to localhost/[0:0:0:0:0:0:0:1]:4318
	at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.kt:297) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.connection.RealConnection.connect(RealConnection.kt:207) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.connection.ExchangeFinder.findConnection(ExchangeFinder.kt:226) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.connection.ExchangeFinder.findHealthyConnection(ExchangeFinder.kt:106) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.connection.ExchangeFinder.find(ExchangeFinder.kt:74) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.connection.RealCall.initExchange$okhttp(RealCall.kt:255) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.kt:32) ~[okhttp-4.12.0.jar:na]
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109) ~[okhttp-4.12.0.jar:na]
```

This is because the OpenTelemetry Protocol (OTLP) exporter is trying to send telemetry data to a local OpenTelemetry Collector instance, but it can't find one running. Let's fix that!

### Running the OpenTelemetry Collector
The [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/) is a component that can receive, process, and export telemetry data from your application. It acts as a bridge between your application and various backends for storing and visualizing telemetry data. Your application is expecting to find one running on the local machine at port 4318, so let's run one! From the root directory of this workshop codebase you'll execute the following command:

```bash
docker run --rm -p 4317:4317 -p 4318:4318 \
  -v "$(pwd)/otel/otel-config.yaml":/otel-config.yaml \
  otel/opentelemetry-collector-contrib:latest \
  --config=/otel-config.yaml
```

Let's look at what this command does:
- `docker run`: This command starts a new Docker container.
- `--rm`: This option tells Docker to remove the container when it stops running.
- `-p 4317:4317 -p 4318:4318`: These options map the container's ports 4317 and 4318 to the host machine's ports 4317 and 4318, respectively. This allows your application to communicate with the OpenTelemetry Collector.
- `-v "$(pwd)/otel/otel-config.yaml":/otel-config.yaml`: This option mounts the `otel-config.yaml` file from your local machine into the container at the path `/otel-config.yaml`. This file contains the configuration for the OpenTelemetry Collector.
- `otel/opentelemetry-collector:latest`: This specifies the Docker image to use for the OpenTelemetry Collector. The `latest` tag ensures that you are using the most recent version of the collector.
- `--config=/otel-config.yaml`: This option tells the OpenTelemetry Collector to use the configuration file mounted at `/otel-config.yaml`.

Now that the OpenTelemetry Collector is running, you should see output in the console indicating that it is receiving telemetry data (logs, traces, and metrics) from your application!

### Observability Visualization
It is beyond the scope of this workshop to set up a full observability stack, but you can visualize the telemetry data collected by OpenTelemetry using various tools. Some popular options include:
- [Grafana](https://grafana.com/): using the [Grafana Tempo](https://grafana.com/oss/tempo/) for tracing, [Grafana Loki](https://grafana.com/oss/loki/) for logs, and [Grafana Prometheus](https://grafana.com/oss/prometheus/) for metrics.
- [Honeycomb](https://www.honeycomb.io/): A commercial observability platform that provides powerful querying and visualization capabilities for traces and metrics.
- [New Relic](https://newrelic.com/): A commercial observability platform that provides comprehensive monitoring and visualization for applications.
- [Datadog](https://www.datadoghq.com/): A commercial observability platform that provides monitoring, logging, and tracing capabilities.

Here's an example of an OpenTelemetry Collector configuration file (`otel-config.yaml`) that you can use to get started with DataDog:

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: "0.0.0.0:4317"
      http:
        endpoint: "0.0.0.0:4318"
      
processors:
  batch:
    send_batch_max_size: 1000
    send_batch_size: 100
    timeout: 10s

connectors:
  datadog/connector:

exporters:
  debug:
    verbosity: normal  # Can be 'normal' or 'detailed'
  datadog:
    api:
      site: "YOUR_DATADOG_SITE"  # e.g., "datadoghq.com", "datadoghq.eu"
      key: "YOUR_DATADOG_API_KEY"  # Replace with your Datadog API key
service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [debug,datadog/connector,datadog]
    metrics:
      receivers: [datadog/connector,otlp]
      processors: [batch]
      exporters: [debug,datadog]
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [debug,datadog]
```

## What's Next?

Now that we have a fully observable Spring Boot application, we can start [preparing for production](preparing-for-production.md)!

