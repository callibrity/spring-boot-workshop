package com.callibrity.spring.workshop.app;

public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);

    PersonDto retrievePersonById(String id);

    PersonDto updatePerson(String id, String firstName, String lastName);

    void deletePersonById(String id);
}
