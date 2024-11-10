package com.g8.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationClassVisitorTest {

    private Map<String, List<String>> map;
    private AnnotationClassVisitor classVisitor;
    private String className;
    private String superName;

    @BeforeEach
    void setUp() {
        map  = new HashMap<>();
        classVisitor = new AnnotationClassVisitor(map);
        className = "com/example/OuterClass";
        superName = "java/lang/Likeable";
    }

    @Test
    public void testVisit_NormalClass() {

        // Call the visit method
        classVisitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, superName, null);

        // Assert the class is not nested
        assertEquals("normalClass", classVisitor.getClassInfo().getClassType());
    }

    @Test
    public void testVisit_NestedClass() {
        // Input values for a nested class
        className += "$InnerClass";

        // Call the visit method
        classVisitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, superName, null);

        // Assert the parent class is mapped correctly
        assertTrue(map.get("com.example.OuterClass").contains("com.example.InnerClass"));
    }

    @Test
    public void testVisit_InterfaceClass() {

        // Call the visit method
        classVisitor.visit(Opcodes.V1_8, Opcodes.ACC_INTERFACE, className, null, superName, null);

        // Assert the class is identified as an interface
        assertEquals("interfaceClass", classVisitor.getClassInfo().getClassType());
    }

    @Test
    public void testVisit_AbstractClass() {

        // Call the visit method
        classVisitor.visit(Opcodes.V1_8, Opcodes.ACC_ABSTRACT, className, null, superName, null);

        // Assert the class is identified as abstract
        assertEquals("abstractClass", classVisitor.getClassInfo().getClassType());
    }

    @Test
    public void testVisit_HandlesInterfaces() {

        // Input values for a class that implements multiple interfaces
        String[] interfaces = {"java/lang/Cloneable", "java/io/Serializable"};

        // Call the visit method
        classVisitor.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, superName, interfaces);

        // Assert interfaces
        assertTrue(classVisitor.getClassInfo().getImplementationList().contains("java.lang.Cloneable"));
    }


    @Test
    public void testVisitMethod() {

        // Visiting a method
        classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "myMethod", "()V", null, null);

        // Verify that the method is added
        assertFalse(classVisitor.getMethodInfoList().isEmpty());
    }

    @Test
    public void testVisitMethodStatic() {

        // Simulate visiting a static method
        classVisitor.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "myStaticMethod", "()V", null, null);
        // Verify that the method is added as static
        assertTrue(classVisitor.getMethodInfoList().get(0).isStatic());
    }

    @Test
    public void testVisitField() {

        // Simulate visiting a field
        classVisitor.visitField(Opcodes.ACC_PRIVATE, "myField", "I", null, null);
        // Verify that the field is added to the list with correct details
        assertFalse(classVisitor.getFieldInfoList().isEmpty());
    }

    @Test
    public void testVisitFieldStatic() {

        // Simulate visiting a static field
        classVisitor.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "myStaticField", "Ljava/lang/String;", null, null);

        // Verify that the field is added as static
        assertTrue(classVisitor.getFieldInfoList().get(0).isStatic());
    }

    @Test
    public void testVisitEnd() {

        // Simulate visiting an annotation
        classVisitor.visitAnnotation("Lorg/springframework/web/bind/annotation/RestController;", true);

        // Finalize the class information
        classVisitor.visitEnd();

        // Verify that classInfo object is loaded with all the information once the class has been visited
        assertFalse(classVisitor.getClassInfo().getAnnotations().isEmpty());
    }

    @Test
    public void testGetFieldType_PrimitiveTypes() {
        // Test for each primitive type descriptor
        assertEquals("int", classVisitor.getFieldType("I"));
    }

    @Test
    public void testGetFieldType_ObjectType() {
        // Test for object type descriptors
        assertEquals("java.util.List", classVisitor.getFieldType("Ljava/util/List;"));
    }

    @Test
    public void testGetFieldType_MultiDimensionalArray() {
        // Test for multi-dimensional array type descriptors
        assertEquals("java.util.List[][]", classVisitor.getFieldType("[[Ljava/util/List;"));
    }

    @Test
    public void testGetFieldType_1DArray() {
        // Test for single-dimensional array types
        assertEquals("int[]", classVisitor.getFieldType("[I"));
    }
}
