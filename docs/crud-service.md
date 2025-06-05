# Creating a Simple CRUD Service

In this section we will create a simple CRUD (Create, Read, Update, Delete) service for managing `Person` domain entities using the "Onion Architecture".

## Creating the Person Domain Entity
First, we need to create a domain entity that represents a `Person`. This entity will be used to model the data we want to manage in our CRUD service. Create a new Java class named `Person` in the `src/main/java/com/callibrity/spring/workshop/domain` directory:

```java
package com.callibrity.spring.workshop.domain;

import java.util.UUID;

public class Person {

    private String id = UUID.randomUUID().toString();
    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void rename(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Person person)) return false;
        return id.equals(person.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}
```

## Creating the Person Repository
Next, we will create a repository interface that will handle data access for our `Person` domain entity. This repository will be responsible for storing and retrieving `Person` objects. Create a new interface named `PersonRepository` in the `src/main/java/com/callibrity/spring/workshop/domain` directory:

```java
package com.callibrity.spring.workshop.domain;

import java.util.Optional;

public interface PersonRepository {
    Person save(Person person);

    Optional<Person> findById(String id);

    void deleteById(String id);
}
```

For now, we will use an in-memory implementation of this repository (we'll use a real database in the next module). Create a new package named `infra` in the `src/main/java/com/callibrity/spring/workshop` directory, and then create a new class named `InMemoryPersonRepository` in that package:
```java
package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class InMemoryPersonRepository implements PersonRepository {
    private final Map<String, Person> persons = new HashMap<>();

    @Override
    public Person save(Person person) {
        persons.put(person.getId(), person);
        return person;
    }

    @Override
    public Optional<Person> findById(String id) {
        return Optional.ofNullable(persons.get(id));
    }

    @Override
    public void deleteById(String id) {
        persons.remove(id);
    }
}
```

This `InMemoryPersonRepository` class implements the `PersonRepository` interface and provides an in-memory storage solution using a `HashMap`. It allows us to save, find, and delete `Person` objects by their `id`. Notice the `@Service` annotation, which indicates that this class is a Spring "bean" and will be automatically detected and managed by Spring's dependency injection.

## Creating the Person DTO 
Let's define a simple data transfer object (DTO) for a `Person` domain entity. Create a new record class named `PersonDto` in the `src/main/java/com/callibrity/spring/workshop/app` directory:

```java
public record PersonDto(String id, String firstName, String lastName) {
}
```

Records in Java are a special kind of class that is used to model immutable data. This `PersonDto` record class will contain automatically-generated accessors for its fields.

_Note: When implementing the onion architecture, DTOs are typically used to transfer data between layers of the application, such as from the service layer to the controller layer. They help decouple the internal domain model from the external API representation._

## Creating the Person Application Service
Next, we will create an application service that will handle the business logic for managing our `Person` domain entities. For now, we will only provide a `createPerson` method. Create a new interface named `PersonService` in the same directory as the `PersonDto` class (`src/main/java/com/callibrity/spring/workshop/app`):

```java
package com.callibrity.spring.workshop.app;

public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);
}
```

as well as a default implementation class named `DefaultPersonService`:

```java
package com.callibrity.spring.workshop.app;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultPersonService implements PersonService {
    private final PersonRepository repository;

    public DefaultPersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public PersonDto createPerson(String firstName, String lastName) {
        var person = new Person(firstName, lastName);
        return mapToDto(repository.save(person));
    }

    private PersonDto mapToDto(Person person) {
        return new PersonDto(person.getId(), person.getFirstName(), person.getLastName());
    }
}
```

Let's cover a few key points about this code:
- The `@Service` Annotation: again, we use this annotation to make this object a Spring bean, which allows it to be automatically detected and managed by Spring's dependency injection.
- Constructor Injection: The `DefaultPersonService` constructor takes a `PersonRepository` as a parameter, which is automatically injected by Spring.

## Unit Testing the Person Service
We are going to write a unit test for our `DefaultPersonService` to ensure that it behaves as expected. To do this, we will be using a fluent assertions API provided by [AssertJ](https://assertj.github.io/doc/#assertj-core) to enhance the readability of our tests, so we'll need to add the AssertJ dependency to our `pom.xml` file. Add the following dependency under the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

Now, we can write a unit test for the `DefaultPersonService`. We will be using [Mockito](https://site.mockito.org/) (this dependency is already provided transiently by the `spring-boot-starter-test` dependency) to create a mock instance of the `PersonRepository` so that we can isolate the service logic from the repository implementation. Let's create a new test class named `DefaultPersonServiceTest` in the `src/test/java/com/callibrity/spring/workshop/app` directory:

```java
package com.callibrity.spring.workshop.app;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Test
    void shouldCreatePerson() {
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var personService = new DefaultPersonService(personRepository);

        var dto = personService.createPerson("John", "Doe");
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNotNull();
        assertThat(dto.firstName()).isEqualTo("John");
        assertThat(dto.lastName()).isEqualTo("Doe");

        verify(personRepository).save(personCaptor.capture());
        verifyNoMoreInteractions(personRepository);

        var personArgument = personCaptor.getValue();
        assertThat(personArgument).isNotNull();
        assertThat(personArgument.getId()).isNotNull();
        assertThat(personArgument.getFirstName()).isEqualTo("John");
        assertThat(personArgument.getLastName()).isEqualTo("Doe");
    }

}
```

Let's break down some of the key components of this test:
- `@ExtendWith(MockitoExtension.class)`: This annotation enables Mockito support in the test class, allowing us to use annotations like `@Mock` and `@Captor`.
- `@Mock`: This annotation creates a mock instance of the `PersonRepository` interface, which we can use to simulate its behavior in the test.
- `@Captor`: This annotation creates an `ArgumentCaptor` that we can use to capture arguments passed to the `save` method of the `PersonRepository`.
- `when(personRepository.save(any(Person.class)))`: This line sets up the mock to return the same `Person` object that is passed to the `save` method, simulating a successful save operation.
- `verify(personRepository).save(personCaptor.capture())`: This line verifies that the `save` method was called on the `PersonRepository` mock and captures the argument passed to it.
- `verifyNoMoreInteractions(personRepository)`: This line ensures that no other interactions occurred with the `PersonRepository` mock after the `save` method was called.
- `assertThat()`: This is an assertion from AssertJ that checks various conditions on the `PersonDto` and the captured `Person` object to ensure they contain the expected values.

## Creating the Person Controller's Create Endpoint
Now, we will need to create a REST controller that will handle HTTP requests for our `Person` domain entity. Create a new class named `PersonController` in the `src/main/java/com/callibrity/spring/workshop/web` directory:

```java
package com.callibrity.spring.workshop.web;

import com.callibrity.spring.workshop.app.PersonDto;
import com.callibrity.spring.workshop.app.PersonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
public class PersonController {
    
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    public PersonDto createPerson(@RequestBody CreatePersonRequest request) {
        return personService.createPerson(request.firstName(),  request.lastName());
    }
    
    public record CreatePersonRequest(String firstName, String lastName) {
        
    }
}
```

Let's explain some of the key components of this code:
- `@RequestMapping("/api/persons")`: This annotation maps all requests to this controller to the `/api/persons` base path.
- `@PostMapping`: This annotation maps HTTP POST requests to the `createPerson` method (at the base path).
- `@RequestBody`: This annotation indicates that the method parameter should be bound to the body of the HTTP request. In this case, it will be deserialized into a `CreatePersonRequest` object.
- `CreatePersonRequest`: This is a record class that represents the request body for creating a new `Person`.

_Note: The `createPerson` method returns a `PersonDto`, which is the data transfer object we defined earlier. This allows us to return a simplified representation of the `Person` entity in the HTTP response._

## Taking the Person Controller for a Spin

Let's go back to Swagger UI (`http://localhost:8080/swagger-ui/index.html`) and test the `createPerson` endpoint we just created. After editing the request body and executing the request, you should see a response similar to the following:
```json
{
  "id": "55673fb1-bac6-468c-a2bf-54d80dbc6da7",
  "firstName": "John",
  "lastName": "Doe"
}
```

This response indicates that the `Person` was successfully created, and it includes the generated `id` and the provided first and last names.

## Writing an Automated Test for the Person Controller

To ensure our `PersonController` works correctly, we can create an automated test using Spring Boot's testing support. Create a new test class named `PersonControllerTest` in the `src/test/java/com/callibrity/spring/workshop/web` directory:


```java
package com.callibrity.spring.workshop.web;

import com.callibrity.spring.workshop.app.PersonDto;
import com.callibrity.spring.workshop.app.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @Test
    void shouldCallPersonServiceCorrectly() throws Exception{
        when(personService.createPerson("John", "Doe"))
                .thenReturn(new PersonDto("1", "John", "Doe"));

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\", \"lastName\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
}
```

Let's break down some of the new concepts in this test:
- `@MockitoBean`: This annotation creates a mock instance of the `PersonService` that is automatically injected into the test context. This allows us to define the behavior of the service without needing a real implementation.
- `post("/api/persons")`: This line specifies that we are performing a POST request to the `/api/persons` endpoint with the provided JSON content.
- `andExpect(jsonPath("$.id").value("1"))`: This line verifies that the JSON response contains an `id` field with the value "1". Similarly, we check the `firstName` and `lastName` fields.

## Creating the Person Controller's Retrieve Endpoint

To add a retrieve endpoint for retrieving a `Person` by `id`, we will extend the `PersonController` class. First, though, we need to update the `PersonService` interface to include a method for retrieving a `Person` by `id`:

```java
public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);
    PersonDto retrievePersonById(String id);
}
```

Now, we can implement the `retrievePersonById` method in the `DefaultPersonService` class:

```java
@Override
public PersonDto retrievePersonById(String id) {
    return repository.findById(id)
            .map(this::mapToDto)
            .orElseThrow(() -> new IllegalArgumentException(String.format("Person with id %s not found", id)));
}
```

Let's explain the new aspects of this code:
- `repository.findById(id)`: This line calls the `findById` method of the `PersonRepository` to retrieve the `Person` by its `id`. This returns an `Optional<Person>`, which may or may not contain a `Person` object.
- `.map(this::mapToDto)`: If a `Person` is found, we map it to a `PersonDto` using the `mapToDto` method we defined earlier.
- `.orElseThrow(...)`: If no `Person` is found, we throw an `IllegalArgumentException` with a message indicating that the `Person` with the specified `id` was not found.

Finally, we will implement a `retrievePerson` method in the `PersonController` class:

```java
@GetMapping("/{id}")
public PersonDto retrievePerson(@PathVariable String id) {
    return personService.retrievePersonById(id);
}
```

Let's explain some of the new concepts in this code:
- `@GetMapping("/{id}")`: This annotation maps HTTP GET requests to the `retrievePerson` method, where `{id}` is a path variable representing the `id` of the `Person` to retrieve.
- `@PathVariable String id`: This annotation binds the path variable `{id}` to the method parameter `id`, allowing us to access the `id` of the `Person` we want to retrieve.

Now, we can test out retrieving a `Person` by `id` using Swagger UI. After creating a `Person` using the `createPerson` endpoint, you can use the `retrievePerson` endpoint to get the details of that `Person`. We should also implement a unit test for this new endpoint:

```java
@Test
void shouldRetrievePerson() throws Exception {
    when(personService.retrievePersonById("1"))
            .thenReturn(new PersonDto("1", "John", "Doe"));

    mockMvc.perform(get("/api/persons/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));

    verify(personService).retrievePersonById("1");
    verifyNoMoreInteractions(personService);
}
```

There are no new concepts in this test, but it verifies that the `retrievePerson` endpoint correctly retrieves a `Person` by its `id` and returns the expected JSON response. But, what happens when something goes wrong?

## Exception Handling in the Person Controller
Inevitably, we will encounter situations where a `Person` with the specified `id` does not exist. To handle this gracefully, we can create an exception handler that will catch the `IllegalArgumentException` thrown by the `retrievePersonById` method and return a response containing a [Problem Detail](https://datatracker.ietf.org/doc/html/rfc7807). To do this, we will create a new class named `ProblemDetailExceptionHandlerAdvice` in the `src/main/java/com/callibrity/spring/workshop/web/error` directory:


```java
package com.callibrity.spring.workshop.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemDetailExceptionHandlerAdvice {
    
    private static final Logger log = LoggerFactory.getLogger(ProblemDetailExceptionHandlerAdvice.class);
    
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception e) {
        log.error("An unhandled exception has occurred.", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
```

Let's break down some of the key components of this exception handler:
- `@RestControllerAdvice`: This annotation indicates that this class is a global exception handler for REST controllers. It allows us to handle exceptions thrown by any controller in the application.
- `@ExceptionHandler(Exception.class)`: This method will handle any unhandled exceptions that occur in the application. It logs the exception and returns a `ProblemDetail` with a 500 Internal Server Error status.
- `@ExceptionHandler(IllegalArgumentException.class)`: This method specifically handles `IllegalArgumentException` exceptions, which we throw when a `Person` with the specified `id` is not found. It returns a `ProblemDetail` with a 400 Bad Request status and the exception message.

Now, when we try to retrieve a `Person` that does not exist, we will receive a 400 Bad Request response with a meaningful error message instead of a generic 500 Internal Server Error. Let's verify this with a unit test in the `PersonControllerTest` class:

```java
@Test
void shouldReturn400WhenPersonNotFound() throws Exception {
    when(personService.retrievePersonById("non-existent-id"))
            .thenThrow(new IllegalArgumentException("Person with id non-existent-id not found"));

    mockMvc.perform(get("/api/persons/non-existent-id"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Person with id non-existent-id not found"));

    verify(personService).retrievePersonById("non-existent-id");
    verifyNoMoreInteractions(personService);
}
```

If we wanted to alternatively return a 404 Not Found (recommended) status rather than a 400 Bad Request, we could introduce a custom exception class, such as `PersonNotFoundException`, and modify the exception handler to return a 404 status for that specific exception:

```java
package com.callibrity.spring.workshop.app;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(String id) {
        super(String.format("Person with id %s not found", id));
    }
}
```

Then, we would modify the `retrievePersonById` method in the `DefaultPersonService` to throw this new exception:

```java
@Override
public PersonDto retrievePersonById(String id) {
    return repository.findById(id)
            .map(this::mapToDto)
            .orElseThrow(() -> new PersonNotFoundException(id));
}
```

And finally, we would update the exception handler to handle this new exception:

```java
@ExceptionHandler(PersonNotFoundException.class)
public ProblemDetail handlePersonNotFoundException(PersonNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
}
```

This way, we can provide a more specific error response when a `Person` is not found, which can be useful for clients consuming our API.

## Creating the Update and Delete Endpoints

At this point, you should have enough understanding to implement the update and delete endpoints for the `PersonController`. I will leave that as an exercise for you (you've got this)!

## What's Next?

In the next module, we will enhance our CRUD service by integrating a real database using the [Java Persistence API](persistence-with-jpa.md)!



