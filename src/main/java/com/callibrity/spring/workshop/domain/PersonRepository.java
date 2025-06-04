package com.callibrity.spring.workshop.domain;

import java.util.Optional;

public interface PersonRepository {
    Person save(Person person);

    Optional<Person> findById(String id);

    void deleteById(String id);
}
