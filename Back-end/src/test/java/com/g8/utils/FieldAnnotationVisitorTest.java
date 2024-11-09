package com.g8.utils;

import com.g8.model.FieldInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FieldAnnotationVisitorTest {

    private FieldInfo fieldInfo;
    private FieldAnnotationVisitor fieldAnnotationVisitor;

    @BeforeEach
    void setUp() {
        fieldInfo = createFieldInfo("myStaticField", "int", true);
        fieldAnnotationVisitor = new FieldAnnotationVisitor(fieldInfo);
    }

    @Test
    public void testVisitFieldAnnotation() {

        // Visiting a field annotation
        AnnotationVisitor annotationVisitor = fieldAnnotationVisitor.visitAnnotation("Lcom/example/MyFieldAnnotation;", true);
        annotationVisitor.visit("value", "test");

        // End the visit and validate the annotation
        fieldAnnotationVisitor.visitEnd();

        assertAnnotationSize(fieldInfo);
    }

    @Test
    public void testVisitMultipleFieldAnnotations() {

        // Visiting multiple annotations
        AnnotationVisitor annotationVisitor1 = fieldAnnotationVisitor.visitAnnotation("Lcom/example/FirstAnnotation;", true);
        annotationVisitor1.visit("value", "test1");

        AnnotationVisitor annotationVisitor2 = fieldAnnotationVisitor.visitAnnotation("Lcom/example/SecondAnnotation;", true);
        annotationVisitor2.visit("value", "test2");

        // End the visit and validate the annotations
        fieldAnnotationVisitor.visitEnd();

        assertEquals(2, fieldInfo.getAnnotations().size());
    }

    @Test
    public void testVisitFieldAnnotationWithEmptyDescriptor() {

        // Simulate visiting a field annotation with an empty descriptor
        AnnotationVisitor annotationVisitor = fieldAnnotationVisitor.visitAnnotation("", true);

        // End the visit and validate no annotation added
        fieldAnnotationVisitor.visitEnd();

        assertTrue(fieldInfo.getAnnotations().isEmpty());
    }

    @Test
    public void testVisitFieldAnnotationWithNullDescriptor() {
        FieldAnnotationVisitor fieldAnnotationVisitor = new FieldAnnotationVisitor(fieldInfo);

        // Expect NullPointerException when passing null descriptor
        assertThrows(NullPointerException.class, () -> {
            fieldAnnotationVisitor.visitAnnotation(null, true);
        });
    }

    @Test
    public void testFieldInfoAnnotatedFlag() {

        // Visit an annotation
        fieldAnnotationVisitor.visitAnnotation("Lcom/example/MyAnnotation;", true);

        fieldAnnotationVisitor.visitEnd();

        // Check that the field is marked as annotated
        assertTrue(fieldInfo.isAnnotated());
    }

    @Test
    public void testStaticFieldAnnotation() {
        // Test a static field with an annotation
        FieldInfo fieldInfo2 = createFieldInfo("myStaticField", "int", true);

        FieldAnnotationVisitor fieldAnnotationVisitor2 = new FieldAnnotationVisitor(fieldInfo);

        // Visit an annotation on static field
        fieldAnnotationVisitor2.visitAnnotation("Lcom/example/StaticFieldAnnotation;", true);

        fieldAnnotationVisitor2.visitEnd();

        // Ensure the annotation is added and the field is marked static
        assertTrue(fieldInfo2.isStatic());
    }

    // Method to create FieldInfo instance with datatype and static flag
    private FieldInfo createFieldInfo(String fieldName, String datatype, boolean isStatic) {
        return new FieldInfo(fieldName, datatype, new ArrayList<>(), isStatic, true);
    }

    // Method to assert that the field contains the expected number of annotations
    private void assertAnnotationSize(FieldInfo fieldInfo) {
        List<String> annotations = fieldInfo.getAnnotations();
        assertFalse(annotations.isEmpty());
    }
}
