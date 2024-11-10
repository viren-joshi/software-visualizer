package com.g8.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DependencyHandlerTest {

    private DependencyHandler dependencyHandler;
    private static final String TEST_CLASS_CONTAINER = "com/blog";
    private static final String TEST_JAR_FILE_NAME = "blog.jar";
    private static final String TEST_JAR_FILE_PATH = "src" + File.separator + "test" + File.separator + "resources" + File.separator + TEST_JAR_FILE_NAME;

    @BeforeEach
    void setUp() {
        dependencyHandler = new DependencyHandler();
        dependencyHandler.setUSER_PACKAGE_PREFIX(TEST_CLASS_CONTAINER);
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
    void testAnalyzeFile() throws Exception {

        dependencyHandler.analyzeFile(TEST_JAR_FILE_PATH);

        assertFalse(dependencyHandler.getClassList().isEmpty());
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

