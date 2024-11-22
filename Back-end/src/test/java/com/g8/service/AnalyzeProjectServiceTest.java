package com.g8.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyzeProjectServiceTest {

    private AnalyzeProjectService analyzeProjectService;
    private static final String TEST_CLASS_CONTAINER = "com/blog";
    private static final String TEST_JAR_FILE_NAME = "blog-0.0.1-SNAPSHOT.jar";
    private static String TEST_JAR_FILE_PATH = "src" + File.separator + "test" + File.separator + "resources" + File.separator + TEST_JAR_FILE_NAME;

    @Mock
    private JarFile jarFile;

    @Mock
    private JarEntry jarEntry;

    @Mock 
    private DependencyRetrievalService dependencyRetrievalService;

    private InputStream pomInputStream;

    @BeforeEach
    void setUp() {
        analyzeProjectService = new AnalyzeProjectService(dependencyRetrievalService);
        analyzeProjectService.setUSER_PACKAGE_PREFIX(TEST_CLASS_CONTAINER);
    }

    @Test
    void testAnalyzePomDependencies_entryIsNull() throws Exception {

        analyzeProjectService.analyzePomDependencies(null, jarFile);

        assertTrue(analyzeProjectService.getExternalForTest().isEmpty());
    }

    @Test
    void testAnalyzePomDependencies_withDependencies() throws Exception {

        pomInputStream = createMockPomStream(true, true, true, true, true);

        when(jarFile.getInputStream(jarEntry)).thenReturn(pomInputStream);

        analyzeProjectService.analyzePomDependencies(jarEntry, jarFile);

        String expected = "[{groupId=org.springframework, scope=compile, artifactId=spring-core, version=5.3.10}]";
        assertEquals(expected, analyzeProjectService.getExternalForTest().toString());
    }

    @Test
    void testAnalyzePomDependencies_withNoDependencies() throws Exception {

        pomInputStream = createMockPomStream(false, false, false, false, false);

        when(jarFile.getInputStream(jarEntry)).thenReturn(pomInputStream);

        analyzeProjectService.analyzePomDependencies(jarEntry, jarFile);

        assertTrue(analyzeProjectService.getExternalForTest().isEmpty());
    }

    @Test
    void testAnalyzePomDependencies_withNullVersionAndScope() throws Exception {

        pomInputStream = createMockPomStream(true ,true, true, false, false);

        when(jarFile.getInputStream(jarEntry)).thenReturn(pomInputStream);

        analyzeProjectService.analyzePomDependencies(jarEntry, jarFile);

        String expected = "[{groupId=org.springframework, scope=, artifactId=spring-core, version=}]";
        assertEquals(expected, analyzeProjectService.getExternalForTest().toString());
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
        analyzeProjectService.saveFile(mockFile, tempFile.getAbsolutePath());

        // Verify the contents of the temporary file match the original content
        byte[] savedFileContent = Files.readAllBytes(tempFilePath);
        assertArrayEquals(fileContent, savedFileContent, "File content should match the input content.");
    }

    @Test
    void testAnalyzeFile_withZeroClasses() throws Exception {

        analyzeProjectService.setUSER_PACKAGE_PREFIX("org/example");
        TEST_JAR_FILE_PATH = "src/test/resources/empty-1.0-SNAPSHOT.jar";

        try (MockedStatic<FirestoreClient> firestoreClient = mockStatic(FirestoreClient.class);
             MockedStatic<DependencyRetrievalService> mockedStatic = mockStatic(DependencyRetrievalService.class)) {

            firestoreClient.when(FirestoreClient::getFirestore).thenReturn(mock(Firestore.class));
            mockedStatic.when(() -> dependencyRetrievalService.saveData(any(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture("mocked response"));

            assertDoesNotThrow(() -> analyzeProjectService.analyzeFile(TEST_JAR_FILE_PATH));
        }
    }

    @Test
    void testAnalyzeUploadedProject_shouldNotThrowException() throws Exception {

        // Mock AnalyzeProjectService object and methods
        File file = new File(TEST_JAR_FILE_PATH);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Creating a MultipartFile from an existing file
        MockMultipartFile mmf = new MockMultipartFile(file.getName(), file.getName(), "application/java-archive", fileInputStream);

        // Verify that saveFile and analyzeFile were called
        try (MockedStatic<FirestoreClient> firestoreClient = mockStatic(FirestoreClient.class);
             MockedStatic<DependencyRetrievalService> mockedStatic = mockStatic(DependencyRetrievalService.class)) {
            // Define the behavior of saveData
            firestoreClient.when(FirestoreClient::getFirestore).thenReturn(mock(Firestore.class));
            mockedStatic.when(() -> dependencyRetrievalService.saveData(any(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture("mocked response"));

            assertDoesNotThrow(() -> analyzeProjectService.analyzeUploadedProject(mmf, TEST_CLASS_CONTAINER));

            Files.delete(Paths.get(TEST_JAR_FILE_NAME));
        }
    }

    @Test
    void testAnalyzeUploadedProject_shouldFail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        ResponseEntity<String> result = analyzeProjectService.analyzeUploadedProject(file, "com.g8.test");
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

