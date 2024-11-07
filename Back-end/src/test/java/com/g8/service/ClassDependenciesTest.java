package com.g8.service;

import com.g8.model.ClassType;
import com.g8.model.UserClass;
import com.g8.model.UserControllerClass;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClassDependenciesTest {

    private DependencyHandler dependencyHandler;
    private CtClass mockCtClass;

    @BeforeEach
    public void setUp() throws NotFoundException, ClassNotFoundException {
        dependencyHandler = new DependencyHandler();
        mockCtClass = mock(CtClass.class);
    }

    // Test for checking interface
    @Test
    public void shouldReturnInterfaceClassType() {

        when(mockCtClass.isInterface()).thenReturn(true);
        ClassType result = dependencyHandler.getClassType(mockCtClass);
        assertEquals(ClassType.interfaceClass, result);
    }

    // Test for abstract class
    @Test
    public void shouldReturnAbstractClassType() {

        when(mockCtClass.isInterface()).thenReturn(false);
        when(mockCtClass.getModifiers()).thenReturn(Modifier.ABSTRACT);

        ClassType result = dependencyHandler.getClassType(mockCtClass);
        assertEquals(ClassType.abstractClass, result);
    }

    // Test for normal class
    @Test
    public void shouldReturnNormalClassType() {

        when(mockCtClass.isInterface()).thenReturn(false);
        when(mockCtClass.getModifiers()).thenReturn(Modifier.PUBLIC);

        ClassType result = dependencyHandler.getClassType(mockCtClass);
        assertEquals(ClassType.normalClass, result);
    }

    // Test for no inheritance
    @Test
    public void testNoInheritance() throws NotFoundException {

        // Mock a CtClass with no superclass (null)
        when(mockCtClass.getSuperclass()).thenReturn(null);

        UserClass userClass = new UserClass();

        dependencyHandler.extractInheritance(mockCtClass, userClass);

        // Verify no inheritance was set
        assertTrue(userClass.inherits.isEmpty());
    }

    // Test for classes with no nested classes
    @Test
    public void testNoNestedClasses() throws Exception {
        // Mock the main CtClass with no nested classes
        CtClass[] nestedClasses = {};

        when(mockCtClass.getDeclaredClasses()).thenReturn(nestedClasses);

        UserClass userClass = new UserClass();

        // Execute the method
        dependencyHandler.extractNestedClasses(mockCtClass, userClass);

        // Verify that nestedClassesList remains empty
        assertTrue(userClass.nestedClassesList.isEmpty());
    }

    // Tests classes without any variables
    @Test
    public void testClassesWithoutFields() throws NotFoundException, ClassNotFoundException {

        UserClass userClass = new UserClass();

        when(mockCtClass.getDeclaredFields()).thenReturn(new CtField[0]);

        dependencyHandler.extractVariables(mockCtClass, userClass);

        assertTrue(userClass.variableList.isEmpty(), "Expected no variables to be extracted.");
    }

    // Test for retrieving a controller class
    @Test
    public void shouldClassifyAControllerClass() throws Exception {

        Object mockAnnotation = mock(Object.class);
        when(mockAnnotation.toString()).thenReturn("@org.springframework.web.bind.annotation.PostMapping");
        when(mockCtClass.getAnnotations()).thenReturn(new Object[]{mockAnnotation});

        UserClass result = dependencyHandler.getUserClass(mockCtClass);

        assertTrue(result instanceof UserControllerClass, "Expected to return a UserControllerClass");
    }

    // Test for retrieving a class that is not a controller
    @Test
    public void shouldClassifyNonControllerClass() throws Exception {

        when(mockCtClass.getAnnotations()).thenReturn(new Object[0]); // No annotations

        UserClass result = dependencyHandler.getUserClass(mockCtClass);

        assertFalse(result instanceof UserControllerClass, "Expected not to be a UserControllerClass");
    }
}
