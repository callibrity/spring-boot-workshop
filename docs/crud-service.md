# Creating a Simple CRUD Service

In this section we will create a simple CRUD (Create, Read, Update, Delete) service for managing `Person` domain entities using the "Onion Architecture".

## Creating the Person Domain Entity
First, we need to create a domain entity that represents a `Person`. This entity will be used to model the data we want to manage in our CRUD service. Create a new Java class named `Person` in the `src/main/java/com/callibrity/spring/workshop/domain` directory:

```java
package com.callibrity.spring.workshop.domain;

import java.util.UU`id`;

public class Person {

    private String id = UU`id`.randomUU`id`().toString();
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
}
```

## Creating the Person Repository
Next, we will create a repository interface that will handle data access for our `Person` domain entity. This repository will be responsible for storing and retrieving `Person` objects. Create a new interface named `PersonRepository` in the `src/main/java/com/callibrity/spring/workshop/domain` directory:

```java
public interface PersonRepository {
    Person save(Person person);
}
```

## Creating the Person DTO 
Let's define a simple data transfer object (DTO) for a `Person` domain entity. Create a new record class named `PersonDto` in the `src/main/java/com/callibrity/spring/workshop/app` directory:

```java
public record PersonDto(String id, String firstName, String lastName) {
}
```

Records in Java are a special kind of class that is used to model immutable data. This `PersonDto` record class will contain automatically-generated accessors for its fields.

***Note: When implementing the onion architecture, DTOs are typically used to transfer data between layers of the application, such as from the service layer to the controller layer. They help decouple the internal domain model from the external API representation.***

## Creating the Person Application Service
Next, we will create an application service that will handle the business logic for our `Person` domain entity. Create a new interface named `PersonService` in the same directory:

```java
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
- The `@Service` Annotation: indicates that this class is a Spring service component, which allows it to be automatically detected and managed by Spring.
- Constructor Injection: The `DefaultPersonService` constructor takes a `PersonRepository` as a parameter, which is automatically injected by Spring.

***Note: Your `id`E will likely be showing an error that the `PersonRepository` interface is not implemented. This is because we haven't created a concrete implementation yet. We'll do that soon!***

## Testing the Person Service
To test our `DefaultPersonService`, we can create a simple unit test. Create a new test class named `DefaultPersonServiceTest` in the `src/test/java/com/callibrity/spring/workshop/app` directory:

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
- `@ExtendWith(MockitoExtension.class)`: This annotation enables [Mockito](https://site.mockito.org/) support in the test class, allowing us to use annotations like `@Mock` and `@Captor`.
- `@Mock`: This annotation creates a mock instance of the `PersonRepository` interface, which we can use to simulate its behavior in the test.
- `@Captor`: This annotation creates an `ArgumentCaptor` that we can use to capture arguments passed to the `save` method of the `PersonRepository`.
- `when(personRepository.save(any(Person.class)))`: This line sets up the mock to return the same `Person` object that is passed to the `save` method, simulating a successful save operation.
- `verify(personRepository).save(personCaptor.capture())`: This line verifies that the `save` method was called on the `PersonRepository` mock and captures the argument passed to it.
- `verifyNoMoreInteractions(personRepository)`: This line ensures that no other interactions occurred with the `PersonRepository` mock after the `save` method was called.
- Assertions: We use [AssertJ](https://assertj.github.io/doc/#assertj-core) assertions to verify that the `PersonDto` returned by the service contains the expected values and that the captured `Person` object has the correct properties.

## Creating the Person Repository Implementation
Next, we will create a concrete implementation of the `PersonRepository` interface. This implementation will use an in-memory data structure to store `Person` objects. Create a new class named `InMemoryPersonRepository` in the `src/main/java/com/callibrity/spring/workshop/infra` directory:

```java
package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InMemoryPersonRepository implements PersonRepository {
    private final Map<String, Person> persons = new HashMap<>();

    @Override
    public Person save(Person person) {
        persons.put(person.getId(), person);
        return person;
    }
}
```

***Note: The error in your `id`E should now be resolved, as we have provided a concrete implementation of the `PersonRepository` interface.***

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

## Testing the CRUD Service
To test our CRUD service, we can use HTTPie (or a similar tool) to send requests to our application. Make sure your Spring Boot application is running, and then you can use the following command to create a new `Person`:

```bash
http POST http://localhost:8080/api/persons firstName="John" lastName="Doe"
```

You should see a response similar to the following:

```text
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 04 Jun 2025 17:26:32 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "firstName": "John",
    "id": "95a310d2-6606-4759-8343-9f57d9e32014",
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

## Creating the Person Controller's Read Endpoint
To add a read endpoint for retrieving a `Person` by `id`, we will extend the `PersonController` class. First, we need to update the `PersonService` interface to include a method for retrieving a `Person` by `id`:

```java
public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);
    PersonDto getPersonById(String id);
}
```

Next, we will need a way to retrieve a `Person` by its `id` from the `PersonRepository`. We will add a new method to the `PersonRepository` interface:

```java
import java.util.Optional;

public interface PersonRepository {
    Person save(Person person);
    Optional<Person> findById(String id);
}
```

Here, we are using `Optional<Person>` to handle the case where a `Person` with the given `id` might not exist. This allows us to avoid returning `null` and instead provide a more explicit way to handle the absence of a value.

We will need to implement this method in the `InMemoryPersonRepository` class:

```java
@Override
public Optional<Person> findById(String id) {
    return Optional.ofNullable(persons.get(id));
}
```

Now, we can implement the `getPersonById` method in the `DefaultPersonService` class (the implementation is an exercise for you):

```java
@Override
public PersonDto getPersonById(String id) {
    // TODO: Implement me!
}
```

***Hint: if you get stuck, you can refer to the `final` branch of the repository for a complete implementation.***

Finally, we will implement the `getPersonById` method in the `PersonController` class. This method will handle GET requests to retrieve a `Person` by its `id`.
In our `PersonController`, we will add a new endpoint to handle GET requests for retrieving a `PersonDto` by `id`:

```java
@GetMapping("/{id}")
public PersonDto getPersonById(@PathVariable String id) {
    return personService.getPersonById(id);
}
```

Let's explain the new aspects of this code:
- `@GetMapping("/{id}")`: This annotation maps HTTP GET requests to the `getPersonById` method, where `{id}` is a path variable representing the `id` of the `Person` to retrieve.
- `@PathVariable String id`: This annotation binds the path variable `{id}` to the method parameter `id`, allowing us to access the `id` of the `Person` we want to retrieve.

You should now be able to test the read endpoint by sending a GET request to `/api/persons/{id}` with a valid `id`. For example:

```bash
http http://localhost:8080/api/persons/1
```

***Note: HTTPie uses GET requests by default, so you don't need to specify the method explicitly.***

## Implementing the Person Controller's Update Endpoint
To add an update endpoint for modifying an existing `Person`, we will extend the `PersonService` and `PersonController` classes. First, we need to update the `PersonService` interface to include a method for updating a `Person`:

```java
public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);
    PersonDto getPersonById(String id);
    PersonDto updatePerson(String id, String firstName, String lastName);
}
```
