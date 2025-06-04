package com.callibrity.spring.workshop.web;

import com.callibrity.spring.workshop.app.PersonDto;
import com.callibrity.spring.workshop.app.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @Test
    void shouldCreatePerson() throws Exception{
        when(personService.createPerson("John", "Doe"))
                .thenReturn(new PersonDto("1", "John", "Doe"));

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\", \"lastName\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(personService).createPerson("John", "Doe");
        verifyNoMoreInteractions(personService);
    }

    @Test
    void shouldRetrievePerson() throws Exception {
        when(personService.getPersonById("1"))
                .thenReturn(new PersonDto("1", "John", "Doe"));

        mockMvc.perform(get("/api/persons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(personService).getPersonById("1");
        verifyNoMoreInteractions(personService);
    }

    @Test
    void shouldUpdatePerson() throws Exception {
        when(personService.updatePerson("1", "Jane", "Doe"))
                .thenReturn(new PersonDto("1", "Jane", "Doe"));

        mockMvc.perform(put("/api/persons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\", \"lastName\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(personService).updatePerson("1", "Jane", "Doe");
        verifyNoMoreInteractions(personService);
    }

    @Test
    void shouldDeletePerson() throws Exception {

        // Assuming the delete operation does not return a body
        doNothing().when(personService).deletePersonById("1");

        mockMvc.perform(delete("/api/persons/1"))
                        .andExpect(status().isOk());

        verify(personService).deletePersonById("1");
        verifyNoMoreInteractions(personService);

    }
}