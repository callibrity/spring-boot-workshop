package com.callibrity.spring.workshop.app;

import org.jwcarman.jpa.pagination.PageDto;
import org.jwcarman.jpa.pagination.PageSpec;

public interface PersonService {
    PersonDto createPerson(String firstName, String lastName);
    PersonDto retrievePersonById(String id);
    PageDto<PersonDto> listPersons(PageSpec pageSpec);
}