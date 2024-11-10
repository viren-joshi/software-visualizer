package com.g8.controller;

import com.g8.service.DependencyHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DependencyHandler dependencyHandler;

    private String baseURL = "http://localhost:8080/initialize";

    @Test
    public void testUploadProjectWithValidJar() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "project.jar", "application/java-archive", "some-content".getBytes());

        Mockito.when(dependencyHandler.analyzeUploadedProject(eq(validFile), eq("com.example")))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.multipart(baseURL + "/upload")
                        .file(validFile)
                        .param("classContainer", "com.example")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }

    @Test
    public void testUploadProjectWithInvalidFileFormat() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "project.txt", MediaType.TEXT_PLAIN_VALUE, "some-content".getBytes());

        Mockito.when(dependencyHandler.analyzeUploadedProject(eq(invalidFile), eq("com.example")))
                .thenReturn(new ResponseEntity<>("Unsupported file", HttpStatus.BAD_REQUEST));

        mockMvc.perform(MockMvcRequestBuilders.multipart(baseURL + "/upload")
                        .file(invalidFile)
                        .param("classContainer", "com.example")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported file"));
    }

    @Test
    public void testUploadProjectWithException() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "project.jar", "application/java-archive", "some-content".getBytes());

        Mockito.when(dependencyHandler.analyzeUploadedProject(any(MockMultipartFile.class), eq("com.example")))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.multipart(baseURL + "/upload")
                        .file(validFile)
                        .param("classContainer", "com.example")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("Failed to analyze project"));
    }

    // Reusing existing tests
    @Test
    public void testGetInternalDependencies() throws Exception {
        testAssertNonEmptyJson("/intDep");
    }

    @Test
    public void testGetClasses() throws Exception {
        testAssertNonEmptyJson("/classList");
    }

    private void testAssertNonEmptyJson(String endpoint) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(baseURL + endpoint)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
