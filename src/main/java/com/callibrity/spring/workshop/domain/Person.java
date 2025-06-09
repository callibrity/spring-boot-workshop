package com.callibrity.spring.workshop.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

@Entity
public class Person {

    @Id
    private String id = UUID.randomUUID().toString();

    @NotEmpty(message="{person.firstName.notEmpty}")
    private String firstName;

    @NotEmpty(message="{person.lastName.notEmpty}")
    private String lastName;

    protected Person() {
        // Default constructor for JPA!
    }

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