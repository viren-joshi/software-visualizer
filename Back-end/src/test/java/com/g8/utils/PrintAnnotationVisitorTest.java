package com.g8.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrintAnnotationVisitorTest {

    private PrintAnnotationVisitor annotationVisitor;

    @BeforeEach
    void setUp() {
        annotationVisitor = new PrintAnnotationVisitor("MyAnnotation");
    }

    @Test
    public void testVisitSimpleKeyValuePair() {
        // Test visiting a simple key-value pair
        annotationVisitor.visit("key", "value");

        // Check if the formatted annotation string is correct
        assertEquals("MyAnnotation(key = value)", annotationVisitor.getFullAnnotation());
    }

    @Test
    public void testVisitNullNameValue() {
        // Test visiting with a null name and a value
        annotationVisitor.visit(null, "value");

        // Check if the value is added correctly
        assertEquals("MyAnnotation(value)", annotationVisitor.getFullAnnotation());
    }

    @Test
    public void testVisitEnumValue() {
        // Test visiting an enum value
        annotationVisitor.visitEnum("enumKey", "Lcom/example/Enum;", "ENUM_CONSTANT");

        // Check if the enum value is formatted correctly
        assertEquals("MyAnnotation(enumKey = com/example/Enum.ENUM_CONSTANT)", annotationVisitor.getFullAnnotation());
    }

    @Test
    public void testVisitEnumNullName() {
        // Test visiting an enum value with null name
        annotationVisitor.visitEnum(null, "Lcom/example/Enum;", "ENUM_CONSTANT");

        // Check if the enum value is added without a name
        assertEquals("MyAnnotation(com/example/Enum.ENUM_CONSTANT)", annotationVisitor.getFullAnnotation());
    }

    @Test
    public void testVisitNestedAnnotation() {
        // Test visiting a nested annotation
        annotationVisitor.visitAnnotation("nestedAnnotation", "Lcom/example/NestedAnnotation;");

        // Check if the nested annotation is formatted correctly
        assertEquals("MyAnnotation(nestedAnnotation = @com.example.NestedAnnotation)", annotationVisitor.getFullAnnotation());
    }

    @Test
    public void testVisitMultipleElements() {
        // Test visiting multiple elements (key-value pairs, enum, nested annotation)
        annotationVisitor.visit("key1", "value1");
        annotationVisitor.visitEnum("enumKey", "Lcom/example/Enum;", "ENUM_CONSTANT");
        annotationVisitor.visitAnnotation("nestedAnnotation", "Lcom/example/NestedAnnotation;");

        // Check if all elements are included in the formatted string
        assertEquals("MyAnnotation(key1 = value1, enumKey = com/example/Enum.ENUM_CONSTANT, nestedAnnotation = @com.example.NestedAnnotation)", annotationVisitor.getFullAnnotation());
    }

    @Test
    public void testGetFullAnnotationWithNoElements() {
        // Test the case where no elements are added
        PrintAnnotationVisitor emptyAnnotationVisitor = new PrintAnnotationVisitor("EmptyAnnotation");

        // Check if it returns the annotation name only
        assertEquals("EmptyAnnotation", emptyAnnotationVisitor.getFullAnnotation());
    }
}
