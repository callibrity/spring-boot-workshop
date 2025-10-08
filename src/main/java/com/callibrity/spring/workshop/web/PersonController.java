package com.callibrity.spring.workshop.web;

import com.callibrity.spring.workshop.app.PersonDto;
import com.callibrity.spring.workshop.app.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jwcarman.jpa.pagination.PageDto;
import org.jwcarman.jpa.spring.web.PageParams;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public PersonDto createPerson(@RequestBody @NotNull @Valid CreatePersonRequest request) {
        return personService.createPerson(request.firstName(), request.lastName());
    }

    @GetMapping("/{id}")
    public PersonDto retrievePerson(@PathVariable String id) {
        return personService.retrievePersonById(id);
    }

    @GetMapping
    public PageDto<PersonDto> listPersons(PageParams params) {
        return personService.listPersons(params);
    }

    public record CreatePersonRequest(
            @NotEmpty String firstName,
            @NotEmpty String lastName) {
    }
}