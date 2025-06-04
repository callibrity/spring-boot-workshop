package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryPersonRepositoryTest {

    @Test
    void shouldSavePerson() {
        var repo = new InMemoryPersonRepository();
        var person = new Person("John", "Doe");
        var savedPerson = repo.save(person);

        assertThat(savedPerson).isNotNull();
        assertThat(savedPerson.getId()).isEqualTo(person.getId());
        assertThat(savedPerson.getFirstName()).isEqualTo("John");
        assertThat(savedPerson.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldBeAbleToFindPersonById() {
        var repo = new InMemoryPersonRepository();
        var person = new Person("Jane", "Doe");
        repo.save(person);
        var foundPerson = repo.findById(person.getId());
        assertThat(foundPerson).isPresent().hasValue(person);
    }

    @Test
    void shouldReturnEmptyWhenPersonNotFound() {
        var repo = new InMemoryPersonRepository();
        var foundPerson = repo.findById("non-existent-id");
        assertThat(foundPerson).isNotPresent();
    }

    @Test
    void shouldDeletePersonById() {
        var repo = new InMemoryPersonRepository();
        var person = new Person("Alice", "Smith");
        repo.save(person);

        repo.deleteById(person.getId());

        var foundPerson = repo.findById(person.getId());
        assertThat(foundPerson).isNotPresent();
    }
}