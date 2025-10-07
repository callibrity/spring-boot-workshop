# Database Persistence with the Java Persistence API (JPA)

The standard way to persist data in Java applications is through the Java Persistence API (JPA). JPA provides a set of annotations and interfaces that allow you to map Java objects to database tables, making it easier to work with relational databases.

## Setting up JPA
To use JPA in your Spring Boot application, you need to add the necessary dependencies to your `pom.xml` file. The most common implementation of JPA is Hibernate, which is included by default in Spring Boot. Here's how to set it up:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

We will also need a database driver. For now, we will use [H2](https://www.h2database.com/html/main.html), an embedded, in-memory database. Let's add that dependency as well:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Using the power of Spring Boot Autoconfiguration, H2 will be detected automatically and configured for use in your application!

## Creating an Entity

We already have a `Person` class that we can use as an entity. To make it a JPA entity, we need to add some annotations to it:

```java
@Entity
public class Person {

    @Id
    private String id = UUID.randomUUID().toString();

    private String firstName;
    private String lastName;

    protected Person() {
        // Default constructor for JPA!
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    // Remaining code omitted for brevity...
}
``` 

Let's take a look at what we needed to change:
- `@Entity`: This annotation marks the class as a JPA entity, which means it will be mapped to a database table (named `PERSON`).
- `@Id`: This annotation marks the `id` field as the primary key of the entity. JPA requires every entity to have a primary key.
- The default constructor is required by JPA for instantiation. We can keep our existing constructor for convenience.

## Creating a Repository

Our code already has a `PersonRepository` interface that it uses, but the infrastructure code that provides the implementation is a purely in-memory implementation. To use JPA, we can simply create a new interface in the `infra` package that leverages Spring Data JPA:

```java
package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPersonRepository extends PersonRepository, JpaRepository<Person,String> {
}
```
By simply extending `JpaRepository`, Spring Data JPA will automatically provide an implementation for us at runtime! This means we can use methods like `save()`, `findById()`, and `findAll()` without having to write any SQL or boilerplate code. We "cheated" a little bit when we structured the methods in `PersonRepository` the way we did, because they exactly match what is provided by `JpaRepository` so we can get them for free. By extending `PersonRepository` this runtime-generated repository implementation will implement the interface we already depend upon for our logic.

If your application is still running (`mvn spring-boot:run`), you can stop it and restart it to pick up the new repository. If you are using Spring Boot DevTools, it will automatically restart the application for you. 

### Gotcha!
When DevTools tried to restart the application, it likely encountered an error like this:

```text
***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in com.callibrity.spring.workshop.app.DefaultPersonService required a single bean, but 2 were found:
	- inMemoryPersonRepository: defined in file [/Users/jcarman/IdeaProjects/spring-workshop/target/classes/com/callibrity/spring/workshop/infra/InMemoryPersonRepository.class]
	- jpaPersonRepository: defined in com.callibrity.spring.workshop.infra.JpaPersonRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration

This may be due to missing parameter name information

Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed
```
This is because we now have two implementations of `PersonRepository` in our classpath: the in-memory one and the JPA one. Spring Boot doesn't know which one to use, so it throws an error. You have a couple of options to resolve this:

1. Mark the `JpaPersonRepository` interface as the primary implementation to use by adding the `@Primary` annotation to it.
2. Delete the `InMemoryPersonRepository` class if you no longer need it (probably the simplest solution at this point).

Once you resolve the issue, you should be able to run the application again without any errors.

## Testing the JPA Repository
The application should be fully operational, now using an in-memory database! You can verify that the application is working as expected using Swagger UI.

_Note: if you'd like to see the SQL statements that JPA is generating, you can add the following property to your `src/main/resources/application.properties` file:

```properties
spring.jpa.show-sql=true
```

When you build the application, and it restarts, you will see the following SQL statements in the console output:

```text
Hibernate: drop table if exists person cascade 
Hibernate: create table person (first_name varchar(255), id varchar(255) not null, last_name varchar(255), primary key (id))
```

Spring Data JPA uses [Hibernate](https://hibernate.org/) as the default implementation, and it will automatically create the necessary tables for your entities based on the annotations you provided. This is a powerful feature that allows you to focus on your domain model without worrying about the underlying database schema. But, what if we want to explicitly control the schema? We can do that too?

## Using a Schema Migration Tool
To manage database schema changes in a more controlled way, you can use a schema migration tool like [Liquibase](https://www.liquibase.org/). Liquibase allows you to version your database schema and apply changes incrementally. To use Liquibase, we need to add the following dependency to our `pom.xml` file:

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

Spring Boot Autoconfiguration will kick in, and Liquibase will automatically be configured for you simply by adding this dependency. However, it will be looking for the master changelog file at `src/main/resources/db/changelog/db.changelog-master.yaml`. Since YAML is the root of all evil, we will use XML instead (it's better supported by Liquibase). Let's configure Spring Boot to use an XML-based master changelog file. In your `src/main/resources/application.properties` file, add the following line:

```properties
spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml
spring.jpa.hibernate.ddl-auto=validate
```

_Note: This will also ensure that Hibernate will validate the database schema against the entity definitions, which is a good practice to catch any discrepancies early._

Now we can create the master changelog file at `src/main/resources/db/changelog/db.changelog-master.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <include file="create-person-table.xml" relativeToChangelogFile="true" />

</databaseChangeLog>
```

This file defines the master changelog that Liquibase will use to apply database changes. It includes a separate file for creating the `person` table, which we will create next. In the same directory, create a file named `create-person-table.xml` with the following content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="1" author="jcarman">
        <createTable tableName="person">
            <column name="id" type="varchar(255)">
                <constraints nullable="false" primaryKey="true" />
            </column>

            <column name="first_name" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="last_name" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

</databaseChangeLog>
```

Now, we should be able to run the application again, and Liquibase will automatically apply the changes defined in the changelog files. You should see output similar to this in the console:

```text
2025-06-05T16:06:03.127-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.database                       : Set default schema name to PUBLIC
2025-06-05T16:06:03.239-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.changelog                      : Creating database history table with name: PUBLIC.DATABASECHANGELOG
2025-06-05T16:06:03.264-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.changelog                      : Reading from PUBLIC.DATABASECHANGELOG
2025-06-05T16:06:03.268-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.snapshot                       : Creating snapshot
2025-06-05T16:06:03.293-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.lockservice                    : Successfully acquired change log lock
2025-06-05T16:06:03.293-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.command                        : Using deploymentId: 9153963037
2025-06-05T16:06:03.293-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.changelog                      : Reading from PUBLIC.DATABASECHANGELOG
2025-06-05T16:06:03.300-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.ui                             : Running Changeset: db/changelog/create-person-table.xml::1::jcarman
2025-06-05T16:06:03.301-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.changelog                      : Table person created
2025-06-05T16:06:03.301-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.changelog                      : ChangeSet db/changelog/create-person-table.xml::1::jcarman ran successfully in 1ms
2025-06-05T16:06:03.304-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : UPDATE SUMMARY
2025-06-05T16:06:03.304-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : Run:                          1
2025-06-05T16:06:03.304-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : Previously run:               0
2025-06-05T16:06:03.304-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : Filtered out:                 0
2025-06-05T16:06:03.304-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : -------------------------------
2025-06-05T16:06:03.304-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : Total change sets:            1
2025-06-05T16:06:03.305-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.util                           : Update summary generated
2025-06-05T16:06:03.305-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.command                        : Update command completed successfully.
2025-06-05T16:06:03.305-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.ui                             : Liquibase: Update has been successful. Rows affected: 1
2025-06-05T16:06:03.306-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.lockservice                    : Successfully released change log lock
2025-06-05T16:06:03.306-04:00  INFO 88253 --- [Spring Boot Workshop] [  restartedMain] liquibase.command                        : Command execution complete
```

## Transaction Demarcation

In a real-world application, you often need to perform multiple database operations as part of a single logical transaction. For example, you might want to create a new person and then update some related data in another table. To do this, you can use Spring's transaction management features. To demarcate a transaction, you can use the `@Transactional` annotation on your service methods. Here's an example:

```java
@Service
public class DefaultPersonService implements PersonService {

    private final PersonRepository repository;

    public DefaultPersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PersonDto createPerson(String firstName, String lastName) {
        var person = new Person(firstName, lastName);
        return mapToDto(repository.save(person));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDto retrievePersonById(String id) {
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }
}
```

Here, the `@Transactional` annotation indicates that the method should be executed within a transaction. If any exception occurs during the execution of the method, the transaction will be rolled back, and no changes will be committed to the database. The `readOnly` attribute can be set to `true` for methods that only read data, which can optimize performance.

## Automatic Entity Validation

Spring Boot allows us to use [Jakarta Bean Validation](https://beanvalidation.org/) to automatically validate our entities before they are persisted to the database. To enable this feature, we need to add the following dependency to our `pom.xml` file:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Now, we can add validation annotations to our `Person` entity:

```java
package com.callibrity.spring.workshop.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

@Entity
public class Person {

    @Id
    private String id = UUID.randomUUID().toString();

    @NotEmpty
    private String firstName;
    
    @NotEmpty
    private String lastName;

    // Remaining code omitted for brevity...
}
```

Now, if we try to create a `Person` with an empty `firstName` or `lastName`, Spring Boot will automatically validate the entity and throw a `ConstraintViolationException`. You can add a new `@RestControllerAdvice` class to handle these exceptions globally:

```java
package com.callibrity.spring.workshop.web.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ConstraintViolationExceptionAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException e) {
        final var messages = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(". "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, messages);
    }
}
```

Once you add this class, any time a `ConstraintViolationException` is thrown, it will return a `400 Bad Request` response with a message detailing the validation errors. However, you will notice that the message isn't very helpful:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "must not be empty",
  "instance": "/api/persons"
}
```

Let's see if we can improve that by adding a custom message to our validation annotations. We can do this by modifying the `Person` entity like so:

```java
@Entity
public class Person {

    @Id
    private String id = UUID.randomUUID().toString();

    @NotEmpty(message="{person.firstName.notEmpty}")
    private String firstName;

    @NotEmpty(message="{person.lastName.notEmpty}")
    private String lastName;
    
    // Remaining code omitted for brevity...
}
```

Next, we need to create a properties file for our validation messages. Create a new file named `ValidationMessages.properties` in the `src/main/resources` directory with the following content:

```properties
person.firstName.notEmpty=First name must not be empty
person.lastName.notEmpty=Last name must not be empty
```

Now, if we try to create a `Person` with an empty `firstName`, the error message will be much more informative:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "First name must not be empty",
  "instance": "/api/persons"
}
```

## What's Next?

Now that we have our persistence layer set up with JPA and Liquibase, we can get rid of the in-memory H2 database and switch to a more realistic setup using [PostgreSQL](switching-to-postgres.md)!

