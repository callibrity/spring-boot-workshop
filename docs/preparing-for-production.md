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

We will need to configure Spring Security, so that it knows how to secure our REST endpoints. To do this, we need to create a Spring Boot configuration class. Create a new class named `SpringBootWorkshopConfig` in the `com.callibrity.spring.workshop` package and add the following code:

```java
package com.callibrity.spring.workshop;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringBootWorkshopConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults()) // Allow CORS preflight requests
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Disable CSRF for API endpoints
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll(); // Allow all actuator endpoints
                    authz.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll(); // Allow Swagger UI and API docs
                    authz.requestMatchers("/api/**").fullyAuthenticated(); // Require authentication for API endpoints
                    authz.anyRequest().denyAll(); // Deny all other requests
                })
                .build();
    }
}
```

Let's break this down:
- The class is marked with `@Configuration`, indicating that it provides programmatic Spring configuration.
- The `SecurityFilterChain` bean configures the security for our application.
- We allow Cross-Origin Resource Sharing (CORS) preflight requests, which are necessary for CORS to work properly.
- We disable Cross-Site Request Forgery (CSRF) protection for API endpoints (`/api/**`) since we are using stateless authentication with JWTs.
- We specify that we want to use OAuth2 Resource Server with JWT authentication.
- We allow all actuator endpoints to be accessed without authentication, which is useful for monitoring and management.
- We permit access to the Swagger UI and API documentation endpoints without authentication.
- We require authentication for all API endpoints under `/api/**`.
- All other requests are denied by default.


Since we are using JWTs, we need to let Spring Security know where we expect our JWTs to come from. We can do this by specifying the issuer URI in our `application.properties` file. Add the following line to your `application.properties`:

```properties
auth0.tenant=callibrity-workshop
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://${auth0.tenant}.us.auth0.com/
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${spring.security.oauth2.resourceserver.jwt.issuer-uri}.well-known/jwks.json
```

Let's break down these properties:

- `auth0.tenant`: This is a custom property that specifies the Auth0 tenant name. You can change this to match your Auth0 tenant.
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`: This property sets the expected issuer URI for the JWTs. It points to the Auth0 tenant's base URL.
- `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`: This property specifies the URI where the JSON Web Key Set (JWKS) can be found. The JWKS is used to verify the signatures of the JWTs issued by Auth0.

_Note: If the `jwks-set-uri` is not specified, your application will attempt to self-configure the JWKS URI based on the issuer URI, which is standard for OAuth2 Resource Servers. We specify it to avoid this startup cost!_

Let's build our application again and run it to ensure that everything is set up correctly

### Gotcha!
Now that we have configured our application to use OAuth2, you will have build failures (specifically test failures) because default Spring Security settings will be applied to all of our `@RestController` tests. Let's turn that off:

```java
@WebMvcTest(controllers = HelloController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class HelloControllerTest {
    // Code omitted for brevity...
}
```

Here, we use the `addFilters = false` annotation to disable the security filters for our tests. This allows us to test our controllers without having to provide a valid JWT token.

And, that's it! Your Spring Boot application is now configured to use OAuth2 for securing your REST API. You can test this by running your application and trying to access one of the `PersonController` endpoints without a valid JWT. You should receive a 401 Unauthorized response, indicating that authentication is required.

## Configuring Cross-Origin Resource Sharing (CORS)

When building a REST API, you may need to allow requests from different origins (domains). This is especially important if your frontend application is hosted on a different domain than your backend API. To allow for configuring CORS, we must provide configuration properties for it. Create a new class named `SpringBootWorkshopProps` in the `com.callibrity.spring.workshop` package and add the following code:

```java
package com.callibrity.spring.workshop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

@ConfigurationProperties(prefix = "workshop")
public class SpringBootWorkshopProps {

    private final CorsConfiguration cors = new CorsConfiguration();

    public CorsConfiguration getCors() {
        return cors;
    }
}
```

By specifying `@ConfigurationProperties(prefix = "workshop")`, we are telling Spring Boot to bind properties with the prefix `workshop` to this class. Nothing happens until we add the `@EnableConfigurationProperties(SpringBootWorkshopProps.class)` annotation to our configuration class:

```java
@Configuration
@EnableConfigurationProperties(SpringBootWorkshopProps.class)
public class SpringBootWorkshopConfig {
    private final SpringBootWorkshopProps props;

    public SpringBootWorkshopConfig(SpringBootWorkshopProps props) {
        this.props = props;
    }

    // Other beans and methods...
}
```

Let's set up some sensible defaults for our CORS configuration. Update the `application.properties` file to include the following properties:


```properties
workshop.cors.allowed-methods=GET,POST,PUT,PATCH,DELETE
workshop.cors.allows-credentials=true
workshop.cors.allowed-headers=*
```
_Note: We do not set the `allowed-origins` property here. That will need to be a runtime property, as it will depend on where your frontend application is hosted._

Here's what these properties do:
- `workshop.cors.allowed-methods`: Specifies the HTTP methods that are allowed for CORS requests. In this case, we allow GET, POST, PUT, PATCH, and DELETE methods.
- `workshop.cors.allows-credentials`: Indicates whether credentials (cookies, authorization headers, or TLS client certificates) are allowed in CORS requests. Setting this to `true` allows credentials to be sent.
- `workshop.cors.allowed-headers`: Specifies the headers that are allowed in CORS requests. The `*` value means all headers are allowed.

Now, we need to tell Spring Boot that we wish to apply these CORS settings to our API endpoints. We can do this by creating a `WebMvcConfigurer` bean in our `SpringBootWorkshopConfig` class:

```java
package com.callibrity.spring.workshop;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(SpringBootWorkshopProps.class)
public class SpringBootWorkshopConfig {

    private final SpringBootWorkshopProps props;

    public SpringBootWorkshopConfig(SpringBootWorkshopProps props) {
        this.props = props;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**").combine(props.getCors());
            }
        };
    }
    // Code omitted for brevity...
}
```

Now, our application is configured to allow CORS requests to the `/api/**` endpoints with the specified methods, credentials, and headers (we'll set the allowed origins at runtime)!

## Building a Container Image
To build an Open Container Initiative (OCI) compliant image for our Spring Boot application, we will use the built-in support provided by the Spring Boot Maven plugin. This allows us to create the OCI image directly from our Maven build process, simplifying the deployment to containerized environments. To build the OCI image, run the following command in your terminal:

```bash
mvn spring-boot:build-image
```

Once this command completes successfully, you will see output similar to the following:

```text
[INFO] Successfully built image 'docker.io/library/spring-boot-workshop:0.0.1-SNAPSHOT'
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.837 s
[INFO] Finished at: 2025-06-08T16:15:19-04:00
[INFO] ------------------------------------------------------------------------
```

This command creates an OCI container image that includes your application and all its dependencies. The image is built using the [Cloud Native Buildpacks](https://buildpacks.io/) technology, which is integrated into the Spring Boot Maven plugin. The resulting image will be tagged with the name of your project and the version specified in your `pom.xml`. You can verify the image by listing your local Docker images:

```bash
docker images
```

You should see an entry for `spring-boot-workshop` with the tag `0.0.1-SNAPSHOT` (or whatever version you specified in your `pom.xml`).

## Externalizing Configuration
In a production environment, it's essential to use [externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) to avoid hardcoding sensitive information and to allow for easy changes without rebuilding the application. Spring Boot provides several ways to externalize configuration, including using environment variables, command-line arguments, and property files. 

The properties defined in your `application.properties` file should only be used for "sensible defaults" and should not contain sensitive information like database credentials or API keys. Instead, you can use environment variables or command-line arguments to override these properties at runtime.

## Virtual Threads

Virtual threads are a new feature in Java that allows for lightweight concurrency, making it easier to write scalable applications. They are part of Project Loom, which aims to simplify the development of concurrent applications by providing a more efficient way to handle threads. To avoid having to write your applications in a "reactive" style, which requires a different way of thinking about concurrency, you can use virtual threads to achieve similar scalability without the complexity. To enable virtual threads in your Spring Boot application, update your `application.properties` file with the following line:

```properties
spring.threads.virtual.enabled=true
```

Now, Tomcat will use virtual threads for handling requests, allowing your application to scale more efficiently without the need for complex reactive programming patterns!