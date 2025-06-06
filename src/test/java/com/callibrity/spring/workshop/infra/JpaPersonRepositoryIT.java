package com.callibrity.spring.workshop.infra;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
class JpaPersonRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private PersonRepository repository;

    @Test
    void shouldSaveAndRetrievePerson() {
        var person = new Person("John", "Doe");
        repository.save(person);

        var retrieved = repository.findById(person.getId());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFirstName()).isEqualTo("John");
        assertThat(retrieved.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // avoid transaction rollback
    void shouldThrowExceptionWhenFirstNameIsEmpty() {
        var person = new Person("", "Doe");

        assertThatThrownBy(() -> repository.save(person))
                .isInstanceOf(TransactionSystemException.class);
    }
}