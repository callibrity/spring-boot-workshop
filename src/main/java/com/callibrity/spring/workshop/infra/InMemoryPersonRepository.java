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
