package com.g8.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DependencyHandlerTest {

    private DependencyHandler dependencyHandler;
    private static final String TEST_CLASS_CONTAINER = "com/blog";
    private static final String TEST_JAR_FILE_NAME = "blog.jar";
    private static String TEST_JAR_FILE_PATH = "src" + File.separator + "test" + File.separator + "resources" + File.separator + TEST_JAR_FILE_NAME;

    @Mock
    private JarFile jarFile;

    @Mock
    private JarEntry jarEntry;

    @Mock
    private Dependency dependency;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        dependencyHandler = new DependencyHandler();
        objectMapper = new ObjectMapper();
        dependencyHandler.setUSER_PACKAGE_PREFIX(TEST_CLASS_CONTAINER);
    }

    @Test
    void testAnalyzePomDependencies_entryIsNull() throws Exception {
        String result = dependencyHandler.analyzePomDependencies(null, jarFile);

        List<Map<String, String>> resultList = objectMapper.readValue(result, List.class);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void testAnalyzePomDependencies_withDependencies() throws Exception {

        when(dependency.getGroupId()).thenReturn("org.springframework");
        when(dependency.getArtifactId()).thenReturn("spring-core");
        when(dependency.getVersion()).thenReturn("5.3.10");
        when(dependency.getScope()).thenReturn("compile");

        String result = dependencyHandler.analyzePomDependencies(jarEntry, jarFile);

        String expected = "[{\"groupId\":\"org.springframework\",\"scope\":\"compile\",\"artifactId\":\"spring-core\",\"version\":\"5.3.10\"}]";
        assertEquals(expected, result);
    }

    @Test
    void testAnalyzePomDependencies_withNoDependencies() throws Exception {

        String result = dependencyHandler.analyzePomDependencies(jarEntry, jarFile);

        List<Map<String, String>> resultList = objectMapper.readValue(result, List.class);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void testAnalyzePomDependencies_withNullVersionAndScope() throws Exception {

        when(dependency.getGroupId()).thenReturn("org.example");
        when(dependency.getArtifactId()).thenReturn("example-artifact");
        when(dependency.getVersion()).thenReturn(null); // null version
        when(dependency.getScope()).thenReturn(null);  // null scope

        String result = dependencyHandler.analyzePomDependencies(jarEntry, jarFile);

        String expected = "[{\"groupId\":\"org.example\",\"scope\":\"\",\"artifactId\":\"example-artifact\",\"version\":\"\"}]";
        assertEquals(expected, result);
    }

    @Test
    void testSaveFile() throws Exception {

        byte[] fileContent = "Sample file content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jar", "application/java-archive", fileContent);

        // Create a temporary file
        Path tempFilePath = Files.createTempFile("test2", ".jar");
        File tempFile = tempFilePath.toFile();
        tempFile.deleteOnExit();

        // Call the saveFile method
        dependencyHandler.saveFile(mockFile, tempFile.getAbsolutePath());

        // Verify the contents of the temporary file match the original content
        byte[] savedFileContent = Files.readAllBytes(tempFilePath);
        assertArrayEquals(fileContent, savedFileContent, "File content should match the input content.");
    }

    @Test
    void testAnalyzeFile_withZeroClasses() throws Exception {

        dependencyHandler.setUSER_PACKAGE_PREFIX("org/example");
        TEST_JAR_FILE_PATH = "src/test/resources/empty-1.0-SNAPSHOT.jar";

        assertDoesNotThrow(() -> dependencyHandler.analyzeFile(TEST_JAR_FILE_PATH));
    }

    @Test
    void testAnalyzeUploadedProject_shouldNotThrowException() throws Exception {

        // Mock DependencyHandler object and methods
        File file = new File(TEST_JAR_FILE_PATH);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Creating a MultipartFile from an existing file
        MockMultipartFile mmf =  new MockMultipartFile(file.getName(), file.getName(), "application/java-archive", fileInputStream);

        // Verify that saveFile and analyzeFile were called
        assertDoesNotThrow(() -> dependencyHandler.analyzeUploadedProject(mmf, TEST_CLASS_CONTAINER));
    }

    @Test
    void testAnalyzeUploadedProject_shouldFail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        ResponseEntity<String> result = dependencyHandler.analyzeUploadedProject(file, "com.g8.test");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}

