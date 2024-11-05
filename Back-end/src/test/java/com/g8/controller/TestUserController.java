package com.g8.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g8.service.DependencyHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
public class TestUserController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DependencyHandler dependencyHandler;

    private String baseURL = "http://localhost:8080/initialize";

    @Test
    public void testGetInternalDependencies() throws Exception {
        testAssertNonEmptyJson("/intDep");
    }

    @Test
    public void testGetExternalDependencies() throws Exception {
        testAssertNonEmptyJson("/extDep");
    }

    @Test
    public void testGetClasses() throws Exception {
        testAssertNonEmptyJson("/classList");
    }

    private void testAssertNonEmptyJson(String endpoint) throws Exception {
        mockMvc.perform(get(baseURL + endpoint)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertFalse(new ObjectMapper().readTree(responseBody).isEmpty(),
                            "Response should not be empty for " + endpoint);
                });
    }
}
