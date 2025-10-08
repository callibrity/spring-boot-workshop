package com.callibrity.spring.workshop.app;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jwcarman.jpa.pagination.PageDto;
import org.jwcarman.jpa.pagination.PageSpec;
import org.jwcarman.jpa.spring.page.Pageables;
import org.jwcarman.jpa.spring.page.Pages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
@Observed(name = "service.person")
public class DefaultPersonService implements PersonService {

    private final PersonRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageDto<PersonDto> listPersons(PageSpec pageSpec) {
        log.info("Listing persons with page spec {}", pageSpec);
        var pageable = Pageables.pageableOf(pageSpec, PersonSortParam.class);
        var page = repository.findAll(pageable);
        var dtoPage = page.map(this::mapToDto);
        return Pages.pageDtoOf(dtoPage);
    }

    @Override
    @Transactional
    public PersonDto createPerson(String firstName, String lastName) {
        log.info("Creating person with first name {} and last name {}", firstName, lastName);
        var person = new Person(firstName, lastName);
        return mapToDto(repository.save(person));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDto retrievePersonById(String id) {
        log.info("Retrieving person with id {}", id);
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    private PersonDto mapToDto(Person person) {
        return new PersonDto(person.getId(), person.getFirstName(), person.getLastName());
    }
}