package com.g8.utils;

import com.g8.model.MethodInfo;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MethodAnnotationVisitorTest {

    @Test
    public void testVisitMethodAnnotation() {
        MethodInfo methodInfo = createMethodInfo("myMethod", false); // Non-static method

        MethodAnnotationVisitor methodAnnotationVisitor = new MethodAnnotationVisitor(methodInfo);

        // Simulate visiting an annotation
        AnnotationVisitor annotationVisitor = methodAnnotationVisitor.visitAnnotation("Lcom/example/MyAnnotation;", true);
        annotationVisitor.visit("value", "test");

        // End the visit and validate the annotation
        methodAnnotationVisitor.visitEnd();

        assertAnnotationSize(methodInfo);
    }

    @Test
    public void testVisitAnotherMethodAnnotation() {
        MethodInfo methodInfo = createMethodInfo("anotherMethod", true); // Static method

        MethodAnnotationVisitor methodAnnotationVisitor = new MethodAnnotationVisitor(methodInfo);

        // Simulate visiting another annotation
        AnnotationVisitor annotationVisitor = methodAnnotationVisitor.visitAnnotation("Lcom/example/AnotherAnnotation;", true);
        annotationVisitor.visit("description", "testAnnotation");

        // End the visit and validate the annotation
        methodAnnotationVisitor.visitEnd();

        assertAnnotationRefactor(methodInfo, "@com.example.AnotherAnnotation(description = testAnnotation)");
    }

    // Creates a MethodInfo instance
    private MethodInfo createMethodInfo(String methodName, boolean isStatic) {
        return new MethodInfo(methodName, new ArrayList<>(), isStatic);
    }

    // Tests the total number of annotations
    private void assertAnnotationSize(MethodInfo methodInfo) {
        List<String> annotations = methodInfo.getAnnotations();
        assertFalse(annotations.isEmpty());
    }

    // Tests the name of the annotation
    private void assertAnnotationRefactor(MethodInfo methodInfo, String expectedAnnotation) {
        List<String> annotations = methodInfo.getAnnotations();
        assertEquals(expectedAnnotation, annotations.get(0));
    }

}