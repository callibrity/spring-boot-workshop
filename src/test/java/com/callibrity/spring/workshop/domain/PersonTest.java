package com.callibrity.spring.workshop.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {
    @Test
    void shouldCreatePersonWithUniqueId() {
        var person = new Person("John", "Doe");
        assertThat(person.getId()).isNotNull();
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldRenamePerson() {
        var person = new Person("John", "Doe");
        person.rename("Jane", "Smith");

        assertThat(person.getFirstName()).isEqualTo("Jane");
        assertThat(person.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldNotBeEqualToAnotherInstance() {
        var person1 = new Person("John", "Doe");
        var person2 = new Person("John", "Doe");

        assertThat(person1).isNotEqualTo(person2);
    }

    @Test
    void shouldBeEqualToItself() {
        var person = new Person("John", "Doe");

        assertThat(person).isEqualTo(person);
    }

    @Test
    void shouldNotBeEqualToNull() {
        var person = new Person("John", "Doe");

        assertThat(person).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        var person = new Person("John", "Doe");

        assertThat(person).isNotEqualTo("Not a person");
    }

    @Test
    void shouldNotHaveSameHashWithAnotherInstance() {
        var person1 = new Person("John", "Doe");
        var person2 = new Person("John", "Doe");

        assertThat(person1.hashCode()).isNotEqualTo(person2.hashCode());
    }
}