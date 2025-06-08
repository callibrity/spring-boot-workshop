# Switching to PostgreSQL

We now have our Spring Boot application running with an in-memory H2 database. However, for a more realistic setup, we will switch to using PostgreSQL as our database. This will allow us to persist data and utilize a more robust database system.

## Adding PostgreSQL Dependency

The first thing we need to do is add the PostgreSQL JDBC driver to our project. Open the `pom.xml` file in the root of your project and add the following dependency inside the `<dependencies>` section:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
We can safely remove the H2 dependency as well, since we will no longer be using it. Look for the following dependency in your `pom.xml` and remove it:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Automatically Starting PostgreSQL with Docker Compose

We can use [Docker Compose](https://docs.docker.com/compose/) to start up containers for dependencies required by our application (like PostgreSQL). Spring Boot has an easy-to-use Docker Compose integration that allows us to manage these dependencies directly from our project. First, we need to add the `spring-boot-docker-compose` dependency to our `pom.xml` file. Add the following dependency inside the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-docker-compose</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

Next, we need to create a Docker Compose configuration file in the root of our project. This file will define the PostgreSQL service that we want to run. Create a new file named `docker-compose.yml` and add the following content:

```yaml
services:
  postgres:
    image: 'postgres:latest'
    environment:
      - 'POSTGRES_DB=spring-boot-db'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=spring-boot-user'
    ports:
      - '5432:5432'
```

Now, when we start our application, Docker Compose will automatically start a PostgreSQL container for us and our application will be automatically configured to connect to it! You should see output like this in the console:

```text
2025-06-05T16:23:22.676-04:00  INFO 89994 --- [Spring Boot Workshop] [  restartedMain] .s.b.d.c.l.DockerComposeLifecycleManager : Using Docker Compose file /Users/jcarman/IdeaProjects/spring-workshop/docker-compose.yaml
2025-06-05T16:23:22.972-04:00  INFO 89994 --- [Spring Boot Workshop] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container spring-workshop-postgres-1  Created
2025-06-05T16:23:22.978-04:00  INFO 89994 --- [Spring Boot Workshop] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container spring-workshop-postgres-1  Starting
2025-06-05T16:23:23.040-04:00  INFO 89994 --- [Spring Boot Workshop] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container spring-workshop-postgres-1  Started
2025-06-05T16:23:23.041-04:00  INFO 89994 --- [Spring Boot Workshop] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container spring-workshop-postgres-1  Waiting
2025-06-05T16:23:23.549-04:00  INFO 89994 --- [Spring Boot Workshop] [utReader-stderr] o.s.boot.docker.compose.core.DockerCli   :  Container spring-workshop-postgres-1  Healthy
```

You may notice, however, that the application will also shut down the PostgreSQL container when it stops. This is because the `spring-boot-docker-compose` dependency is configured to stop the containers when the application stops. If you want to keep the PostgreSQL container running after the application stops, you can add the following property to your `src/main/resources/application.properties` file:

```properties
spring.docker.compose.lifecycle-management=start_only
```

## Integration Testing with Testcontainers

To ensure that our application works correctly with PostgreSQL, we can use [Testcontainers](https://www.testcontainers.org/) for integration testing. Testcontainers allows us to run tests against a real PostgreSQL instance in a Docker container. As always, we need to add some new dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

Let's test that our `JpaPersonRepository` works correctly with PostgreSQL. Create a new test class named `JpaPersonRepositoryIT` (integration test) in the `src/test/java/com/callibrity/spring/workshop/infra` directory:

```java
package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
class JpaPersonRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private PersonRepository repository;

    @Test
    void shouldSaveAndRetrievePerson() {
        var person = new Person("John", "Doe");
        repository.save(person);

        var retrieved = repository.findById(person.getId());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFirstName()).isEqualTo("John");
        assertThat(retrieved.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // avoid transaction rollback
    void shouldThrowExceptionWhenFirstNameIsEmpty() {
        var person = new Person("", "Doe");

        assertThatThrownBy(() -> repository.save(person))
                .isInstanceOf(TransactionSystemException.class);
    }
}
```

There's quite a bit going on here:
- `@DataJpaTest`: This annotation sets up an Spring Boot test which focuses on JPA components.
- `@Testcontainers`: This annotation enables Testcontainers support for the test class.
- `@Container`: This annotation creates a PostgreSQL container that will be used for the tests.
- `@ServiceConnection`: This annotation indicates that this container should be treated as a service connection, allowing Spring Boot to automatically configure the datasource for the tests.
- `@Transactional(propagation = Propagation.NOT_SUPPORTED)`: This annotation is used to avoid transaction rollback for the test that checks for invalid input.

## What's Next?

Now that we have an application that uses a real database, we need to be able to [observe and monitor](observability.md) its behavior.