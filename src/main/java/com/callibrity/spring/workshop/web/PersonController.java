package com.callibrity.spring.workshop.web;

import com.callibrity.spring.workshop.app.PersonDto;
import com.callibrity.spring.workshop.app.PersonService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping
    public PersonDto createPerson(@RequestBody CreatePersonRequest request) {
        return personService.createPerson(request.firstName(), request.lastName());
    }

    @GetMapping("/{id}")
    public PersonDto retrievePerson(@PathVariable String id) {
        return personService.getPersonById(id);
    }

    @PutMapping("/{id}")
    public PersonDto updatePerson(@PathVariable String id, @RequestBody UpdatePersonRequest request) {
        return personService.updatePerson(id, request.firstName(),  request.lastName());
    }

    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable String id) {
        personService.deletePersonById(id);
    }

    public record CreatePersonRequest(String firstName, String lastName) {

    }

    public record UpdatePersonRequest(String firstName, String lastName) {

    }
}
