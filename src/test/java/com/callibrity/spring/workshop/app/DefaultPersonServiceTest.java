package com.callibrity.spring.workshop.app;

import com.callibrity.spring.workshop.domain.Person;
import com.callibrity.spring.workshop.domain.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Test
    void shouldCreatePerson() {
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        var personService = new DefaultPersonService(personRepository);

        var dto = personService.createPerson("John", "Doe");
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isNotNull();
        assertThat(dto.firstName()).isEqualTo("John");
        assertThat(dto.lastName()).isEqualTo("Doe");

        verify(personRepository).save(personCaptor.capture());
        verifyNoMoreInteractions(personRepository);

        var personArgument = personCaptor.getValue();
        assertThat(personArgument).isNotNull();
        assertThat(personArgument.getId()).isNotNull();
        assertThat(personArgument.getFirstName()).isEqualTo("John");
        assertThat(personArgument.getLastName()).isEqualTo("Doe");
    }

}