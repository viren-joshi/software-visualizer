package com.g8.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g8.model.ClassType;
import com.g8.model.ClassVariable;
import com.g8.model.UserClass;
import com.g8.model.UserControllerClass;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DependencyHandlerTest {

    private DependencyHandler dependencyHandler;
    private MultipartFile mockFile;
    private CtClass mockCtClass;
    private CtField mockCtField;
    private final String classContainer = "com.blog";

    @BeforeEach
    public void setUp() throws NotFoundException, ClassNotFoundException {
        dependencyHandler = new DependencyHandler();
        mockFile = mock(MultipartFile.class);
        mockCtClass = mock(CtClass.class);
        mockCtField = mock(CtField.class);

        when(mockCtField.getType()).thenReturn(mock(CtClass.class));
        when(mockCtField.getName()).thenReturn("fieldWithAnnotation");
        when(mockCtField.getModifiers()).thenReturn(0);
        when(mockCtField.getAnnotations()).thenReturn(new Object[]{mock(Object.class)});
        when(mockCtClass.getDeclaredFields()).thenReturn(new CtField[]{mockCtField});
        when(mockCtField.getType().getName()).thenReturn("java.lang.String");
    }

    @Test
    public void testAnalyzeUploadedProjectSuccess() throws Exception {

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
    public void testFileNotFound() {
        assertThrows(Exception.class, () -> {
            dependencyHandler.analyzeUploadedProject(null, classContainer);
        });
    }

    // Invalid package name test
    @Test
    public void testInvalidContainer() throws Exception {

        when(mockFile.getOriginalFilename()).thenReturn("blog.jar");
        when(mockFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/blog.jar"));

        // Set a class container that does not exist in the JAR file
        String invalidClassContainer = "invalid.package.name.";

        dependencyHandler.analyzeUploadedProject(mockFile, invalidClassContainer);

        // Check that userProject has no internal dependencies
        assertTrue(dependencyHandler.getUserProject().getUserClassList().isEmpty(), "Expected no user classes in the project as the package not found.");
    }

    // Test for a controller class
    @Test
    public void testGetUserClass_WithPostMappingAnnotation() throws Exception {
        
        Object mockAnnotation = mock(Object.class);
        when(mockAnnotation.toString()).thenReturn("@org.springframework.web.bind.annotation.PostMapping");
        when(mockCtClass.getAnnotations()).thenReturn(new Object[]{mockAnnotation});

        UserClass result = dependencyHandler.getUserClass(mockCtClass);

        assertTrue(result instanceof UserControllerClass, "Expected to return a UserControllerClass");
    }

    // Test for not a controller class
    @Test
    public void testGetUserClass_WithoutPostMappingAnnotation() throws Exception {
        
        when(mockCtClass.getAnnotations()).thenReturn(new Object[0]); // No annotations

        UserClass result = dependencyHandler.getUserClass(mockCtClass);

        assertFalse(result instanceof UserControllerClass, "Expected not to be a UserControllerClass");
    }

    // Test for field with annotation
    @Test
    public void testExtractVariables_WithAnnotatedField() throws ClassNotFoundException, NotFoundException {
        
        UserClass userClass = new UserClass();

        when(mockCtField.getName()).thenReturn("fieldWithAnnotation");

        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertEquals("fieldWithAnnotation", extractedVariable.identifier, "Expected the identifier to be annotated");
    }

    // Test for static field
    @Test
    public void testExtractVariables_WithStaticField() throws NotFoundException, ClassNotFoundException {
        
        UserClass userClass = new UserClass();

        when(mockCtField.getModifiers()).thenReturn(Modifier.STATIC);

        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertTrue(extractedVariable.isStatic, "Expected identifier to be static.");
    }

    // Test for no annotation field
    @Test
    public void testExtractVariables_WithNonAnnotatedField() throws NotFoundException, ClassNotFoundException {
        
        UserClass userClass = new UserClass();

        when(mockCtField.getAnnotations()).thenReturn(new Object[0]);
        
        dependencyHandler.extractVariables(mockCtClass, userClass);
        
        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertFalse(extractedVariable.isAnnotated, "Expected field to NOT be annotated.");
    }

    // Test for no variables in a class
    @Test
    public void testExtractVariables_WithoutFields() throws NotFoundException, ClassNotFoundException {
        
        UserClass userClass = new UserClass();

        when(mockCtClass.getDeclaredFields()).thenReturn(new CtField[0]);
        
        dependencyHandler.extractVariables(mockCtClass, userClass);
        
        assertEquals(0, userClass.variableList.size(), "Expected no variables to be extracted.");
    }

    // Test for the data type of the variables
    @Test
    public void testExtractVariablesWithSameDatatype() throws NotFoundException, ClassNotFoundException {
        
        UserClass userClass = new UserClass();

        when(mockCtField.getType().getName()).thenReturn("java.lang.Double");
        
        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertEquals("java.lang.Double", extractedVariable.datatype, "Expected datatype to match.");
    }

    // Test for annotation parsing
    @Test
    public void testGetEndpointFromAnnotation_withSimpleValue() {
        String annotation = "value=\"/api/test\"";
        String result = dependencyHandler.getEndpointFromAnnotation(annotation);
        assertEquals("/api/test", result, "Should extract /api/test from the annotation string.");
    }

    // Test for interface
    @Test
    public void testGetClassType_InterfaceClass() {

        when(mockCtClass.isInterface()).thenReturn(true);

        ClassType result = dependencyHandler.getClassType(mockCtClass);
        assertEquals(ClassType.interfaceClass, result);
    }

    // Test for abstract class
    @Test
    public void testGetClassType_AbstractClass() {

        when(mockCtClass.isInterface()).thenReturn(false);
        when(mockCtClass.getModifiers()).thenReturn(Modifier.ABSTRACT);

        ClassType result = dependencyHandler.getClassType(mockCtClass);
        assertEquals(ClassType.abstractClass, result);
    }

    // Test for normal class
    @Test
    public void testGetClassType_NormalClass() {

        when(mockCtClass.isInterface()).thenReturn(false);
        when(mockCtClass.getModifiers()).thenReturn(Modifier.PUBLIC);

        ClassType result = dependencyHandler.getClassType(mockCtClass);
        assertEquals(ClassType.normalClass, result);
    }

    // Test for no inheritance
    @Test
    public void testExtractInheritance_NoSuperclass() throws NotFoundException {

        // Mock a CtClass with no superclass (null)
        when(mockCtClass.getSuperclass()).thenReturn(null);

        UserClass userClass = new UserClass();

        // Execute the method
        dependencyHandler.extractInheritance(mockCtClass, userClass);

        // Verify no inheritance was set
        assertEquals("", userClass.inherits);
    }

    // Test for classes with no nested classes
    @Test
    public void testExtractNestedClasses_NoNestedClasses() throws Exception {
        // Mock the main CtClass with no nested classes
        CtClass[] nestedClasses = {};

        when(mockCtClass.getDeclaredClasses()).thenReturn(nestedClasses);

        UserClass userClass = new UserClass();

        // Execute the method
        dependencyHandler.extractNestedClasses(mockCtClass, userClass);

        // Verify that nestedClassesList remains empty
        assertEquals(0, userClass.nestedClassesList.size());
    }

    @Test
    public void testGetInternalDependencies() throws JsonProcessingException {
        assertJsonNotEmpty(() -> dependencyHandler.getInternalDependencies());
    }

    @Test
    public void testGetClassList() throws JsonProcessingException {
        assertJsonNotEmpty(() -> dependencyHandler.getClassList());
    }

    @Test
    public void testGetExternalDependencies() throws JsonProcessingException {
        assertJsonNotEmpty(() -> dependencyHandler.getExternalDependencies());
    }

    private void assertJsonNotEmpty(Supplier<String> jsonSupplier) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = jsonSupplier.get();
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        assertFalse(jsonNode.isEmpty(), "JSON response should not be empty.");
    }

}
