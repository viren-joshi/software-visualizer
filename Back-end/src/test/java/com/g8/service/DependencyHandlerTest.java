package com.g8.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final String TEST_JAR_FILE_NAME = "blog-0.0.1-SNAPSHOT.jar";
    private static String TEST_JAR_FILE_PATH = "src" + File.separator + "test" + File.separator + "resources" + File.separator + TEST_JAR_FILE_NAME;

    @Mock
    private JarFile jarFile;

    @Mock
    private JarEntry jarEntry;

    private InputStream pomInputStream;

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

        pomInputStream = createMockPomStream(true, true, true, true, true);

        when(jarFile.getInputStream(jarEntry)).thenReturn(pomInputStream);

        String result = dependencyHandler.analyzePomDependencies(jarEntry, jarFile);

        String expected = "[{\"groupId\":\"org.springframework\",\"scope\":\"compile\",\"artifactId\":\"spring-core\",\"version\":\"5.3.10\"}]";
        assertEquals(expected, result);
    }

    @Test
    void testAnalyzePomDependencies_withNoDependencies() throws Exception {

        pomInputStream = createMockPomStream(false, false, false, false, false);

        when(jarFile.getInputStream(jarEntry)).thenReturn(pomInputStream);

        String result = dependencyHandler.analyzePomDependencies(jarEntry, jarFile);

        List<Map<String, String>> resultList = objectMapper.readValue(result, List.class);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void testAnalyzePomDependencies_withNullVersionAndScope() throws Exception {

        pomInputStream = createMockPomStream(true ,true, true, false, false);

        when(jarFile.getInputStream(jarEntry)).thenReturn(pomInputStream);

        String result = dependencyHandler.analyzePomDependencies(jarEntry, jarFile);

        String expected = "[{\"groupId\":\"org.springframework\",\"scope\":\"\",\"artifactId\":\"spring-core\",\"version\":\"\"}]";
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

        Files.delete(Paths.get(TEST_JAR_FILE_NAME));
    }

    @Test
    void testAnalyzeUploadedProject_shouldFail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        ResponseEntity<String> result = dependencyHandler.analyzeUploadedProject(file, "com.g8.test");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    private InputStream createMockPomStream(boolean exists, boolean groupId, boolean artifactId, boolean version, boolean scope) {

        String grp = "", artifact = "", ver = "", sc = "", depstart = "", depend = "";

        if(exists) {
            depstart = "<dependency>";
            depend = "</dependency>";
        }
        if(groupId) {
            grp = "<groupId>org.springframework</groupId>";
        }

        if(artifactId) {
            artifact = "<artifactId>spring-core</artifactId>";
        }

        if(version) {
            ver = "<version>5.3.10</version>";
        }

        if(scope) {
            sc = "<scope>compile</scope>";
        }

        String pomXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" +
                        "<modelVersion>4.0.0</modelVersion>" +
                        "<dependencies>" +
                        depstart +
                        grp +
                        artifact +
                        ver +
                        sc +
                        depend +
                        "</dependencies>" +
                        "</project>";
        return new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
    }
}

