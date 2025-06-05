package com.callibrity.spring.workshop.app;

public class PersonNotFoundException extends RuntimeException {
    public PersonNotFoundException(String id) {
        super(String.format("Person with id %s not found", id));
    }
}
