package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPersonRepository extends PersonRepository, JpaRepository<Person,String> {
}