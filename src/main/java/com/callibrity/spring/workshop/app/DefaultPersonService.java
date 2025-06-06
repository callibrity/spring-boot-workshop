package com.callibrity.spring.workshop.app;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultPersonService implements PersonService {

    private final PersonRepository repository;

    public DefaultPersonService(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PersonDto createPerson(String firstName, String lastName) {
        var person = new Person(firstName, lastName);
        return mapToDto(repository.save(person));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDto retrievePersonById(String id) {
        return mapToDto(findById(id));
    }

    @Override
    @Transactional
    public PersonDto updatePerson(String id, String firstName, String lastName) {
        var person = findById(id);
        person.rename(firstName, lastName);
        return mapToDto(repository.save(person));
    }

    @Override
    @Transactional
    public void deletePersonById(String id) {
        repository.deleteById(id);
    }

    private Person findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    private PersonDto mapToDto(Person person) {
        return new PersonDto(person.getId(), person.getFirstName(), person.getLastName());
    }
}
