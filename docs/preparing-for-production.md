# Preparing for Production

To prepare your Spring Boot application for production, there are several best practices and configurations you should consider. This guide will walk you through:

- Securing your application with OAuth2
- Building a container image for your application
- Externalizing configuration
- Using Health Probes for Self-Healing


## Securing Your Application with OAuth2
To secure your Spring Boot application, you can use OAuth2 for authentication and authorization. This is a common practice in modern applications to ensure that only authorized users can access certain resources. Let's configure our REST service to serve as an OAuth2 Resource Server. The first thing we need to do is add the necessary dependencies to our `pom.xml` file. Add the following dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```


## Building a Container Image

To build an Open Container Initiative (OCI) compliant image for our Spring Boot application, we will use the built-in support provided by the Spring Boot Maven plugin. This allows us to create the OCI image directly from our Maven build process, simplifying the deployment to containerized environments. To build the OCI image, run the following command in your terminal:

```bash
mvn spring-boot:build-image
```

Once this command completes successfully, you will see output similar to the following:

```text
[INFO]
[INFO] Successfully built image 'docker.io/library/spring-boot-workshop:0.0.1-SNAPSHOT'
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.985 s
[INFO] Finished at: 2025-06-05T22:50:15-04:00
[INFO] ------------------------------------------------------------------------
```

This command creates an OCI container image that includes your application and all its dependencies. The image is built using the [Cloud Native Buildpacks](https://buildpacks.io/) technology, which is integrated into the Spring Boot Maven plugin. The resulting image will be tagged with the name of your project and the version specified in your `pom.xml`. You can verify the image by listing your local Docker images:

```bash
docker images
```

## JSON Logging Configuration

When running in a production environment, it's essential to have structured logging for better observability and integration with log management systems. Spring Boot supports JSON logging out of the box. To enable JSON logging, you can set the following configuration property:

```properties
logging.structured.format.console=ecs
```

This configuration sets the logging format to ECS (Elastic Common Schema), which is a widely adopted standard for structured logging. With this setting, your application will output logs in JSON format, making it easier to parse and analyze logs in production environments. It is *NOT* recommended to enable this in your `application.properties` file directly for development or testing environments, as it can make logs harder to read. Instead, consider environment variables to enable structured logging in production. You can test this by setting local environment variable and running your application:

```bash
export LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs
mvn spring-boot:run
```

You should now see your logs in JSON format in the console output. This structured logging will help you integrate with various log management solutions like ELK Stack, Splunk, or any other system that supports JSON log ingestion.

_Note: Don't forget to clear that environment variable!_

## Enabling Spring Boot Actuator

To monitor and manage your Spring Boot application in production, you should enable the Spring Boot Actuator. The Actuator provides production-ready features such as health checks, metrics, and application information. To enable it, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

After adding this dependency and restarting your application, you can access the Actuator endpoints at `http://localhost:8080/actuator`. Some of the useful endpoints include:
- `/actuator/health`: Provides health status of the application.
- `/actuator/metrics`: Exposes various metrics about the application.
- `/actuator/info`: Displays application information such as version and build details.
After adding the dependency, you can configure which endpoints are exposed by default in your `application.properties` file:

```properties
management.endpoints.web.exposure.include=*
```

