package com.g8.service;

import com.g8.model.ClassVariable;
import com.g8.model.UserClass;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClassFieldTest {

    private DependencyHandler dependencyHandler;
    private CtClass mockCtClass;
    private CtField mockCtField;

    @BeforeEach
    public void setUp() throws NotFoundException, ClassNotFoundException {
        dependencyHandler = new DependencyHandler();
        mockCtClass = mock(CtClass.class);
        mockCtField = mock(CtField.class);

        when(mockCtField.getType()).thenReturn(mock(CtClass.class));
        when(mockCtField.getName()).thenReturn("fieldWithAnnotation");
        when(mockCtField.getModifiers()).thenReturn(0);
        when(mockCtField.getAnnotations()).thenReturn(new Object[]{mock(Object.class)});
        when(mockCtClass.getDeclaredFields()).thenReturn(new CtField[]{mockCtField});
        when(mockCtField.getType().getName()).thenReturn("java.lang.String");
    }

    // Test for field with annotation
    @Test
    public void testAnnotatedField() throws ClassNotFoundException, NotFoundException {

        UserClass userClass = new UserClass();

        when(mockCtField.getName()).thenReturn("fieldWithAnnotation");

        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertEquals("fieldWithAnnotation", extractedVariable.identifier, "Expected the identifier to be annotated");
    }

    // Test for retrieving static field
    @Test
    public void testStaticField() throws NotFoundException, ClassNotFoundException {

        UserClass userClass = new UserClass();

        when(mockCtField.getModifiers()).thenReturn(Modifier.STATIC);

        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertTrue(extractedVariable.isStatic, "Expected identifier to be static.");
    }

    // Test for retrieving fields without annotations
    @Test
    public void testNotAnnotatedField() throws NotFoundException, ClassNotFoundException {

        UserClass userClass = new UserClass();

        when(mockCtField.getAnnotations()).thenReturn(new Object[0]);

        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertFalse(extractedVariable.isAnnotated, "Expected field to NOT be annotated.");
    }

    // Test for checking the data type of the variables
    @Test
    public void testDatatypeOfFields() throws NotFoundException, ClassNotFoundException {

        UserClass userClass = new UserClass();

        when(mockCtField.getType().getName()).thenReturn("java.lang.Double");

        dependencyHandler.extractVariables(mockCtClass, userClass);

        ClassVariable extractedVariable = userClass.variableList.get(0);
        assertEquals("java.lang.Double", extractedVariable.datatype, "Expected datatype to match.");
    }
}
