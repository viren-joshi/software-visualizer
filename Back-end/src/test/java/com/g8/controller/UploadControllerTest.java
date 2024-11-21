package com.g8.controller;

import com.g8.service.AnalyzeProjectService;
import com.g8.service.AuthService;
import com.g8.service.DependencyRetrievalService;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyzeProjectService analyzeProjectService;

    @MockBean
    private AuthService authService;

    private String baseURL = "http://localhost:8080/initialize";
    private final String authorizationToken = "mock-token";
    private final String projectId = "mock-id";

    @BeforeEach
    public void setup() {
        Mockito.when(authService.verifyToken(authorizationToken)).thenReturn(true);
    }

    @Test
    public void testUploadProjectWithValidJar() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "project.jar", "application/java-archive", "some-content".getBytes());

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Success", HttpStatus.OK);

        Mockito.when(analyzeProjectService.analyzeUploadedProject(eq(validFile), eq("com.example")))
                .thenReturn(responseEntity);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(validFile);
        builder.param("classContainer", "com.example");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }

    @Test
    public void testUploadProjectWithInvalidFileFormat() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "project.txt", MediaType.TEXT_PLAIN_VALUE, "some-content".getBytes());
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Unsupported file", HttpStatus.BAD_REQUEST);

        Mockito.when(analyzeProjectService.analyzeUploadedProject(eq(invalidFile), eq("com.example")))
                .thenReturn(responseEntity);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(invalidFile);
        builder.param("classContainer", "com.example");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unsupported file"));
    }

    @Test
    public void testUploadProjectWithException() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "project.jar", "application/java-archive", "some-content".getBytes());
        RuntimeException runtimeException = new RuntimeException("Unexpected error");

        Mockito.when(analyzeProjectService.analyzeUploadedProject(any(MockMultipartFile.class), eq("com.example")))
                .thenThrow(runtimeException);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(validFile);
        builder.param("classContainer", "com.example");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
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

    @Test
    public void testGetExternalDependencies() throws Exception {
        testAssertNonEmptyJson("/extDep");
    }

    private void testAssertNonEmptyJson(String endpoint) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + endpoint);
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", projectId);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        try (MockedStatic<FirestoreClient> firestoreClient = mockStatic(FirestoreClient.class);
             MockedStatic<DependencyRetrievalService> mockedStatic = mockStatic(DependencyRetrievalService.class)) {

            firestoreClient.when(FirestoreClient::getFirestore).thenReturn(mock(Firestore.class));

            mockMvc.perform(builder)
                    .andExpect(status().isOk());
        }
    }
}
