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

## Testing the REST API
Now, open your web browser or a tool like Postman and navigate to `http://localhost:8080/hello`. You should see the response:

```text
Hello, Spring Boot!
```