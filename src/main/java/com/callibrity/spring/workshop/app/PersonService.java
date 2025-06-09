package com.callibrity.spring.workshop.app;

public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);
    PersonDto retrievePersonById(String id);
}