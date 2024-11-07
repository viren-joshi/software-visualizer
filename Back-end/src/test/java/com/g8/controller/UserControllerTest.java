package com.g8.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g8.model.ExternalDependency;
import com.g8.model.UserClass;
import com.g8.model.UserProject;
import com.g8.service.DependencyHandler;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DependencyHandler dependencyHandler;

    private String baseURL = "http://localhost:8080/initialize";

    @BeforeEach
    public void setUp() {

        UserProject tempProject = new UserProject();

        // Mock data for user class list and external dependencies
        tempProject.getInternalDependencyList().add(new UserClass());
        tempProject.getExternalDependencyList().add(new ExternalDependency());

        when(dependencyHandler.getUserProject()).thenReturn(tempProject);
        when(dependencyHandler.getInternalDependencies()).thenReturn(new Gson().toJson(tempProject.getInternalDependencyList()));
        when(dependencyHandler.getExternalDependencies()).thenReturn(new Gson().toJson(tempProject.getExternalDependencyList()));
        when(dependencyHandler.getClassList()).thenReturn(new Gson().toJson(tempProject.getInternalDependencyList()));
    }

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
