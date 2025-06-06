# Creating a REST API

## Add Spring Boot Web Starter Dependency
To create a REST API, we first need to add a dependency to our project. Spring Boot provides "starters" that make it easy to get started with common functionality. For a web application, we will use the `spring-boot-starter-web` dependency. Add the following dependency to your `pom.xml` file under the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

***Note: Inside your IDE you may need to refresh the Maven project to download the new dependency.***

## Create a REST Controller
Now, let's create a simple REST controller that will handle HTTP requests. Create a new Java class named `HelloController` in the `src/main/java/com/callibrity/spring/workshop` directory:

```java
package com.callibrity.spring.workshop;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Spring Boot!";
    }
}
```

Let's break down what this code does:
- `@RestController`: This annotation indicates that this class is a REST controller, which allows it to handle HTTP requests and return responses directly.
- `@GetMapping("/hello")`: This annotation maps HTTP GET requests to the `sayHello` method. When a request is made to `/hello`, this method will be invoked.

## Run the Application Again
Now that we have our REST controller set up, we can test it. Let's run our application again:

```bash
mvn spring-boot:run
```

You should see similar output as before, indicating that the application has started successfully:

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.0)

2025-06-04T11:55:01.159-04:00  INFO 56433 --- [Spring Workshop] [           main] c.c.s.w.SpringWorkshopApplication        : Starting SpringWorkshopApplication using Java 23.0.2 with PID 56433 (/Users/jcarman/IdeaProjects/spring-workshop/target/classes started by jcarman in /Users/jcarman/IdeaProjects/spring-workshop)
2025-06-04T11:55:01.160-04:00  INFO 56433 --- [Spring Workshop] [           main] c.c.s.w.SpringWorkshopApplication        : No active profile set, falling back to 1 default profile: "default"
2025-06-04T11:55:01.362-04:00  INFO 56433 --- [Spring Workshop] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2025-06-04T11:55:01.367-04:00  INFO 56433 --- [Spring Workshop] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2025-06-04T11:55:01.367-04:00  INFO 56433 --- [Spring Workshop] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.41]
2025-06-04T11:55:01.379-04:00  INFO 56433 --- [Spring Workshop] [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2025-06-04T11:55:01.379-04:00  INFO 56433 --- [Spring Workshop] [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 201 ms
2025-06-04T11:55:01.467-04:00  INFO 56433 --- [Spring Workshop] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2025-06-04T11:55:01.470-04:00  INFO 56433 --- [Spring Workshop] [           main] c.c.s.w.SpringWorkshopApplication        : Started SpringWorkshopApplication in 0.418 seconds (process running for 0.507)
```

Notice, however, that this time the application doesn't immediately terminate as it did before. That's because Spring Boot has started an embedded web server ([Apache Tomcat](https://tomcat.apache.org/)) to handle the incoming HTTP requests and route them to our controller. Let's verify that our REST API is working correctly!

## Taking the REST API for a Spin

Now that our application is running, we can test our REST API. To do that, we'll use [Swagger UI](https://swagger.io/tools/swagger-ui/), which will be automatically provided by [Springdoc](https://springdoc.org/). First, we need to add the Springdoc dependency to our `pom.xml` file:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

When using Maven, most projects will configure the dependency versions using properties. Here's how you can do that in your `pom.xml`:

```xml
<properties>
    <springdoc.version>2.8.8</springdoc.version>
</properties>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

After restarting your application, you can access Swagger UI by navigating to the following URL in your web browser:

```
http://localhost:8080/swagger-ui/index.html
```

To interact with your service, click the "Try it out" button on the `/hello` endpoint and then click the "Execute" button. You should see a plain text response like this:

```text
Hello, Spring Boot!
```

## Live Reloading with Spring Boot Developer Tools
To avoid restarting the application every time you make a change to the code, you can use [Spring Boot DevTools](https://docs.spring.io/spring-boot/reference/using/devtools.html). This tool provides features like automatic restarts and live reload. To add it, include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional> <!-- Don't include in production builds -->
</dependency>
```

After adding this dependency, you can make changes to your code, and the application will automatically restart without needing to run `mvn spring-boot:run` again. This speeds up development significantly! Let's test it out by changing our `HelloController` to return a different message:

```java
@GetMapping("/hello")
public String sayHello() {
    return "Welcome to the Spring Boot Workshop!";
}
```

After saving the changes and recompiling, you can refresh the Swagger UI page or make a request to `/hello` again, and you should see the updated message:

```text
Welcome to the Spring Boot Workshop!
```

## Creating an Automated Test for the REST API
To ensure our REST API works correctly, we can create an automated test using Spring Boot's testing support. Create a new test class named `HelloControllerTest` in the `src/test/java/com/callibrity/spring/workshop` directory:

```java
package com.callibrity.spring.workshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HelloController.class)
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHelloMessage() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring Boot!"));
    }
}
```

This test class does the following:
- `@WebMvcTest`: This annotation tells Spring Boot to bootstrap a web-only test context for the application.
- `@AutoConfigureMockMvc`: This annotation enables the use of `MockMvc`, which allows us to perform HTTP requests in tests without starting a real server.
- `@Autowired`: This annotation injects the `MockMvc` instance, which we can use to perform requests.
- `shouldReturnHelloMessage`: This test method performs a GET request to `/hello` and verifies that the response status is 200 OK and the content matches "Hello, Spring Boot!".

## Running the Tests
To run the tests, you can use your IDE's built-in test runner or execute the following Maven command:

```bash
mvn test
```

You should see output indicating that the test passed successfully:

```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.749 s
[INFO] Finished at: 2025-06-04T12:19:35-04:00
[INFO] ------------------------------------------------------------------------
```

## What's Next?

Now that we have a basic REST API set up, we can implement a more complex service, such as a [CRUD API](crud-service.md) for managing resources.
