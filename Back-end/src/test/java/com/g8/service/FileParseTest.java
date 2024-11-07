package com.g8.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g8.model.ExternalDependency;
import com.g8.model.UserClass;
import com.g8.model.UserProject;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileParseTest {

    private DependencyHandler dependencyHandler;
    private MultipartFile mockFile;

    // standard we are using for our test jar files. All the jar files will have the same container name.
    private final String classContainer = "com.blog";

    @BeforeEach
    public void setUp() throws NotFoundException, ClassNotFoundException {
        dependencyHandler = new DependencyHandler();
        mockFile = mock(MultipartFile.class);
    }

    @Test
    public void analyzeUploadedProjectSuccessfully() throws Exception {

        when(mockFile.getOriginalFilename()).thenReturn("blog.jar");
        when(mockFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/blog.jar"));

        String result = dependencyHandler.analyzeUploadedProject(mockFile, classContainer);

        assertEquals("Project uploaded and analyzed successfully", result);
    }

    // File extension test
    @Test
    public void testInvalidFileExtension() throws Exception {

        when(mockFile.getOriginalFilename()).thenReturn("blog.txt");

        String result = dependencyHandler.analyzeUploadedProject(mockFile, classContainer);

        assertEquals("The uploaded file is not a JAR file. Please upload a valid JAR file.", result);
    }

    // File not found test
    @Test
    public void shouldThrowExceptionForFileNotFound() {
        assertThrows(Exception.class, () -> {
            dependencyHandler.analyzeUploadedProject(null, classContainer);
        });
    }

    // Invalid package name test
    @Test
    public void testInvalidClassContainerName() throws Exception {

        when(mockFile.getOriginalFilename()).thenReturn("blog.jar");
        when(mockFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/blog.jar"));

        // Set a class container that does not exist in the JAR file
        String invalidClassContainer = "invalid.package.name.";

        dependencyHandler.analyzeUploadedProject(mockFile, invalidClassContainer);

        // Check that userProject has no internal dependencies
        assertTrue(dependencyHandler.getUserProject().getInternalDependencyList().isEmpty(), "Expected no user classes in the project as the package not found.");
    }

    // Test for annotation parsing
    @Test
    public void shouldExtractEndpointFromAnnotation() {
        String annotation = "value=\"/api/test\"";
        String result = dependencyHandler.getEndpointFromAnnotation(annotation);
        assertEquals("/api/test", result, "Should extract /api/test from the annotation string.");
    }

    // Tests internal dependency retrieval
    @Test
    public void shouldRetrieveInternalDependencies() throws JsonProcessingException {
        assertJsonNotEmpty(() -> dependencyHandler.getInternalDependencies());
    }

    // Tests class list retrieval
    @Test
    public void shouldRetrieveClassList() throws JsonProcessingException {
        assertJsonNotEmpty(() -> dependencyHandler.getClassList());
    }

    // Tests external dependency retrieval
    @Test
    public void shouldRetrieveExternalDependencies() throws JsonProcessingException {
        assertJsonNotEmpty(() -> dependencyHandler.getExternalDependencies());
    }

    // Helper function for retrieval
    private void assertJsonNotEmpty(Supplier<String> jsonSupplier) throws JsonProcessingException {

        UserProject tempProject = new UserProject();

        // Mock data for user class list and external dependencies
        tempProject.getInternalDependencyList().add(new UserClass());
        tempProject.getExternalDependencyList().add(new ExternalDependency());
        tempProject.getClassNames().add("com.org.main");
        dependencyHandler.setUserProject(tempProject);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = jsonSupplier.get();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        assertFalse(jsonNode.isEmpty(), "JSON response can not be empty because even an empty project has at least one class otherwise making jar wouldn't be possible.");
    }

}