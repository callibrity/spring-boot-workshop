package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;

@Primary
public interface JpaPersonRepository extends PersonRepository , JpaRepository<Person,String> {
}
