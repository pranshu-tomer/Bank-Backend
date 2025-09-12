package com.voltrex.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voltrex.bank.dto.RegisterRequest;
import com.voltrex.bank.entities.Gender;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    private RegisterRequest buildValidRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("alice@example.com");
        req.setPhone("1234567890");
        req.setDob(LocalDate.of(1995, 1, 1));
        req.setStreet("123 Main St");
        req.setCity("Cityville");
        req.setState("CA");
        req.setPincode("90001");
        req.setGender(Gender.FEMALE);
        req.setAge(28);
        return req;
    }

    @Test
    void whenValidRequest_thenUserSaved() throws Exception {
        RegisterRequest req = buildValidRequest();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Check user persisted
        User saved = userRepository.findByEmail(req.getEmail()).orElseThrow();
        assertThat(saved.getFirstName()).isEqualTo("Alice");
        assertThat(saved.getAddress().getCity()).isEqualTo("Cityville");
        assertThat(saved.getStatus().name()).isEqualTo("PENDING");
    }

    @Test
    void whenInvalidEmail_thenBadRequest() throws Exception {
        RegisterRequest req = buildValidRequest();
        req.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenMissingFirstName_thenBadRequest() throws Exception {
        RegisterRequest req = buildValidRequest();
        req.setFirstName(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenInvalidPhone_thenBadRequest() throws Exception {
        RegisterRequest req = buildValidRequest();
        req.setPhone("abc"); // fails regex

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenMissingGender_thenBadRequest() throws Exception {
        RegisterRequest req = buildValidRequest();
        req.setGender(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenNegativeAge_thenBadRequest() throws Exception {
        RegisterRequest req = buildValidRequest();
        req.setAge(-5);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenLastNameMissing_thenStillSaved() throws Exception {
        RegisterRequest req = buildValidRequest();
        req.setLastName(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User saved = userRepository.findByEmail(req.getEmail()).orElseThrow();
        assertThat(saved.getLastName()).isNull();
    }
}


