package com.g8.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g8.service.AnalyzeProjectService;
import com.g8.service.AuthService;
import com.g8.service.DependencyRetrievalService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
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

    @MockBean
    private DependencyRetrievalService dependencyRetrievalService;

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

        Mockito.when(analyzeProjectService.analyzeUploadedProject(eq(validFile), eq("com.example"), any()))
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

        Mockito.when(analyzeProjectService.analyzeUploadedProject(eq(invalidFile), eq("com.example"), any()))
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

        Mockito.when(analyzeProjectService.analyzeUploadedProject(any(MockMultipartFile.class), eq("com.example"), any()))
                .thenThrow(runtimeException);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(validFile);
        builder.param("classContainer", "com.example");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testGetInternalDependencies() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/intDep");
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", projectId);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        CompletableFuture<String> mockInternalDeps = CompletableFuture.completedFuture("{\"internalDeps\": []}");
        Mockito.when(dependencyRetrievalService.getInternalDependencies(projectId))
                .thenReturn(mockInternalDeps);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"internalDeps\": []}"));
    }

    @Test
    public void testGetClasses() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/classList")
                .header("Authorization", authorizationToken)
                .header("project_id", projectId);

        CompletableFuture<String> mockClassList = CompletableFuture.completedFuture("{\"classList\": []}");
        Mockito.when(dependencyRetrievalService.getClassList(projectId))
                .thenReturn(mockClassList);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"classList\": []}"));
    }

    @Test
    public void testGetExternalDependencies() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/extDep")
                .header("Authorization", authorizationToken)
                .header("project_id", projectId);

        CompletableFuture<String> mockExternalDeps = CompletableFuture.completedFuture("{\"externalDeps\": []}");
        Mockito.when(dependencyRetrievalService.getExternalDependencies(projectId))
                .thenReturn(mockExternalDeps);

        mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"externalDeps\": []}"));
    }

    @Test
    public void testUnauthorizedAccessForUpload() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "project.jar", "application/java-archive", "some-content".getBytes());

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(validFile);
        builder.param("classContainer", "com.example");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        Mockito.when(authService.verifyToken(any())).thenReturn(false);

        mockMvc.perform(builder)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized API access"));
    }

    @Test
    public void testUnauthorizedAccessForInternalDependencies() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/intDep");
        builder.header("project_id", projectId);
        builder.header("Authorization", "invalid-token");

        Mockito.when(authService.verifyToken(any())).thenReturn(false);

        mockMvc.perform(builder)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized API access"));
    }

    @Test
    public void testUploadProjectWithEmptyFile() throws Exception {

        MockMultipartFile emptyFile = new MockMultipartFile("file", "project.jar", "application/java-archive", new byte[0]);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(emptyFile);
        builder.param("classContainer", "com.example");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty. Please upload a valid file."));
    }

    @Test
    public void testUploadProjectWithMissingClassContainer() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "project.jar", "application/java-archive", "some-content".getBytes());

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(baseURL + "/upload").file(validFile);
        builder.param("classContainer", "");
        builder.header("Authorization", authorizationToken);
        builder.contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Class container is required."));
    }

    @Test
    public void testInternalDependenciesWithException() throws Exception {
        Mockito.when(dependencyRetrievalService.getInternalDependencies(any()))
                .thenThrow(new RuntimeException("Mock error"));

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/intDep");
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", projectId);

        mockMvc.perform(builder)
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testExternalDependenciesWithException() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/extDep");
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", projectId);

        Mockito.when(dependencyRetrievalService.getExternalDependencies(any()))
                .thenThrow(new RuntimeException("mock error"));

        mockMvc.perform(builder)
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testClassListWithException() throws Exception {

        String mockResponseBody = "Failed to retrieve the class list";

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/classList");
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", projectId);

        Mockito.when(dependencyRetrievalService.getClassList(any()))
                .thenThrow(new RuntimeException(mockResponseBody));

        mockMvc.perform(builder)
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(mockResponseBody));
    }

    @Test
    public void testGetInternalDependenciesWithMissingProjectId() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/intDep");
        builder.header("Authorization", authorizationToken);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetExternalDependenciesWithMissingProjectId() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/extDep");
        builder.header("Authorization", authorizationToken);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInternalDependenciesWithEmptyProjectId() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/intDep");
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", "");

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetExternalDependenciesWithEmptyProjectId() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/extDep");
        builder.header("Authorization", authorizationToken);
        builder.header("project_id", "");

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetClassListWithEmptyProjectId() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/classList");
        builder.header("Authorization", authorizationToken);
        builder.param("project_id", "");

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetClassListWithMissingProjectId() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/classList");
        builder.header("Authorization", authorizationToken);

        mockMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
        public void testGetUserProjects() throws Exception {
        // Mock the user ID and the authorization token
        String mockUserId = "mock-user-id";
        String mockAuthorizationToken = "mock-token";

        // Mock a response for user projects
        List<Map<String, Object>> mockProjects = List.of(
                Map.of("projectId", "1", "projectName", "Project A"),
                Map.of("projectId", "2", "projectName", "Project B")
        );
        String mockResponseJson = new Gson().toJson(mockProjects);

        // Mock the AuthService and DependencyRetrievalService behavior
        Mockito.when(authService.verifyToken(mockAuthorizationToken)).thenReturn(true);
        Mockito.when(authService.getUserId(mockAuthorizationToken)).thenReturn(mockUserId);
        Mockito.when(dependencyRetrievalService.getUserProjects(mockUserId))
                .thenReturn(CompletableFuture.completedFuture(mockProjects));

        // Perform the GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/initialize/userProjects")
                .header("Authorization", mockAuthorizationToken))
                .andExpect(status().isOk())
                .andExpect(content().json(mockResponseJson));
        }

        @Test
        void testCreateCustomView_Success() throws Exception {
            String mockUserId = "mock-user-id";
            String mockAuthorizationToken = "mock-token";
            Map<String, Object> data = Map.of("key", "value", "projectId", projectId);
            String requestBody = new ObjectMapper().writeValueAsString(data);
            String responseMessage = "customView123";

            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(baseURL + "/createCustomView");
            builder.header("Authorization", authorizationToken);
            builder.contentType(MediaType.APPLICATION_JSON);
            builder.content(requestBody);
            builder.param("projectId", projectId);

            Mockito.when(authService.verifyToken(mockAuthorizationToken)).thenReturn(true);
            Mockito.when(authService.getUserId(mockAuthorizationToken)).thenReturn(mockUserId);
            Mockito.when(dependencyRetrievalService.createCustomView(mockUserId, projectId, data))
                    .thenReturn(CompletableFuture.completedFuture(responseMessage));

            mockMvc.perform(builder)
                    .andExpect(status().isOk())
                    .andExpect(content().string(responseMessage));
        }

        @Test
        void testCreateCustomView_Unauthorized() throws Exception {
            reset(authService);
            String projectId = "mock-project-id";
            Map<String, Object> data = Map.of("key", "value", "projectId", projectId);
            String requestBody = new ObjectMapper().writeValueAsString(data);

            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(baseURL + "/createCustomView");
            builder.header("Authorization", authorizationToken);
            builder.contentType(MediaType.APPLICATION_JSON);
            builder.content(requestBody);
            builder.param("projectId", projectId);

            Mockito.when(authService.verifyToken(authorizationToken)).thenReturn(false);

            mockMvc.perform(builder)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Unauthorized API access"));
        }

        @Test
        void testCreateCustomView_Exception() throws Exception {
		    String projectId = "mock-project-id";
        	Map<String, Object> data = Map.of("key", "value", "projectId", projectId);
            String requestBody = new ObjectMapper().writeValueAsString(data);

            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(baseURL + "/createCustomView");
            builder.header("Authorization", authorizationToken);
            builder.contentType(MediaType.APPLICATION_JSON);
            builder.content(requestBody);
            builder.param("projectId", projectId);

            Mockito.when(authService.verifyToken(authorizationToken)).thenReturn(true);
            Mockito.when(authService.getUserId(authorizationToken)).thenReturn("testUser");
            Mockito.when(dependencyRetrievalService.createCustomView("testUser", projectId, data))
                            .thenThrow(new RuntimeException(""));

            mockMvc.perform(builder)
                            .andExpect(status().isInternalServerError())
                            .andExpect(content().string(""));
        }

        @Test
        void testGetCustomView_Success() throws Exception {
            String customViewId = "customView123";
            Map<String, Object> data = Map.of("key", "value", "projectId", projectId);
            String jsonResponse = new Gson().toJson(data);

            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/getCustomView");
            builder.header("Authorization", authorizationToken);
            builder.param("customViewId", customViewId);

            Mockito.when(authService.verifyToken(authorizationToken)).thenReturn(true);
            Mockito.when(dependencyRetrievalService.getCustomViewData(customViewId))
                    .thenReturn(CompletableFuture.completedFuture(data));

            mockMvc.perform(builder)
                    .andExpect(status().isOk())
                    .andExpect(content().json(jsonResponse));
        }

        @Test
        void testGetCustomView_Unauthorized() throws Exception {
            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/getCustomView");
            builder.header("Authorization", authorizationToken);
            builder.param("customViewId", "customView123");

            Mockito.when(authService.verifyToken(authorizationToken)).thenReturn(false);

            mockMvc.perform(builder)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Unauthorized API access"));
        }

        @Test
        void testGetCustomView_Exception() throws Exception {
            String customViewId = "customView123";

            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(baseURL + "/getCustomView");
            builder.header("Authorization", authorizationToken);
            builder.param("customViewId", "customView123");

            Mockito.when(authService.verifyToken(authorizationToken)).thenReturn(true);
            Mockito.when(dependencyRetrievalService.getCustomViewData(customViewId))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(builder)
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Service error"));
        }


}