package com.callibrity.spring.workshop.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person,String>  {

// -------------------------- OTHER METHODS --------------------------

    List<Person> findByLastName(String lastName);


}
