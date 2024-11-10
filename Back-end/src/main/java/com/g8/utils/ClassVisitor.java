package com.g8.utils;

import com.g8.model.ClassInfo;
import com.g8.model.FieldInfo;
import com.g8.model.MethodInfo;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

public class ClassVisitor extends org.objectweb.asm.ClassVisitor {

    private ClassInfo classInfo;
    private List<MethodInfo> methodInfoList;
    private List<FieldInfo> fieldInfoList;
    Map<String, List<String>> parentClassToNestedClassesMap;
    private final List<PrintAnnotationVisitor> annotations;  // Temporary list


    public ClassVisitor(Map<String, List<String>> map) {
        super(Opcodes.ASM9);
        fieldInfoList = new ArrayList<>();
        methodInfoList = new ArrayList<>();
        classInfo = new ClassInfo();
        annotations = new ArrayList<>();
        parentClassToNestedClassesMap = map;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        // split with /
        name = name.replace("/", ".");
        String[] nameArr = name.split("\\.");

        // get the final name that might be combined
        String combinedName = nameArr[nameArr.length - 1];

        // parent name is null
        String parentName = null;

        // if the class is a nested class then it will contain $ between class names
        if(combinedName.contains("$")) {
            String p = combinedName.substring(0, combinedName.indexOf("$"));
            nameArr[nameArr.length - 1] = p;
            parentName = String.join(".", nameArr);
            nameArr[nameArr.length - 1] = combinedName.substring(combinedName.indexOf("$") + 1);
            name = String.join(".", nameArr);
        }

        // setting the name
        classInfo.setName(name);

        // If the superName is not Object, set the inherits
        classInfo.setInherits("java/lang/Object".equals(superName) ? "" : superName.replace('/', '.'));
        if(interfaces != null) {
            List<String> updatedInterfaces = Arrays.stream(interfaces)
                    .map(interfaceName -> interfaceName.replace('/', '.'))
                    .collect(Collectors.toList());
            classInfo.setImplementationList(updatedInterfaces);
        }

        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            classInfo.setClassType("interfaceClass");
        } else if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            classInfo.setClassType("abstractClass");
        } else {
            classInfo.setClassType("normalClass");
        }

        // Detect if the class is a nested class and add it to the parent class
        if (parentName != null) {

            // Add this nested class to the parent's nested classes list
            parentClassToNestedClassesMap
                    .computeIfAbsent(parentName, k -> new ArrayList<>())
                    .add(name);  // Add the nested class to its parent's list

            classInfo.setIsNested(true);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        // Skip the 'init' method (constructor) by checking the method name
        if ("<init>".equals(name) || name.startsWith("lambda$")) {
            return null;  // Returning null means we ignore this method
        }

        MethodInfo currentMethod = new MethodInfo();
        currentMethod.setMethodName(name);
        currentMethod.setStatic((access & Opcodes.ACC_STATIC) != 0); // Check if the method is static

        // Add method to methodList
        methodInfoList.add(currentMethod);
        return new MethodAnnotationVisitor(currentMethod);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        // Create FieldInfo object for this field
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.setIdentifier(name);  // Set the field name (identifier)

        // Extract datatype from the descriptor (e.g., "Ljava/lang/String;" becomes "java.lang.String", "I" becomes "integer)
        String fieldType = getFieldType(descriptor); // Get the last part

        fieldInfo.setDatatype(fieldType); // Set the datatype

        // Determine if the field is static
        fieldInfo.setStatic((access & Opcodes.ACC_STATIC) != 0);

        // Add the field info to the list
        fieldInfoList.add(fieldInfo);

        return new FieldAnnotationVisitor(fieldInfo);  // Return a visitor to collect annotations
    }


    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String annotationName = descriptor.replace('/', '.').replace(";", "");
        annotationName = "@" + annotationName.substring(1);

        String[] ann = annotationName.split("\\.");
        if(ann[ann.length - 1].toLowerCase().contains("controller"))
            classInfo.setIsControllerClass(true);

        PrintAnnotationVisitor annotationVisitor = new PrintAnnotationVisitor(annotationName);
        annotations.add(annotationVisitor);
        return annotationVisitor;
    }

    public ClassInfo getClassInfo() {
        classInfo.setVariableList(fieldInfoList);
        classInfo.setMethodList(methodInfoList);
        return classInfo;
    }

    @Override
    public void visitEnd() {

        for (PrintAnnotationVisitor annotationVisitor : annotations) {
            String fullAnnotation = annotationVisitor.getFullAnnotation();
            classInfo.getAnnotations().add(fullAnnotation);
        }
        annotations.clear();  // Reset list for the next visit
    }

    public String getFieldType(String descriptor) {

        // Handle primitive types directly
        String fieldType = descriptor.replace('/', '.').replace(";", "");

        // Handle array types
        if (descriptor.startsWith("[")) {
            while (fieldType.startsWith("[")) {
                fieldType = fieldType.substring(1); // Remove array notation
                fieldType += "[]";
            }
        }

        if(fieldType.startsWith("L"))
            fieldType = fieldType.substring(1);

        String replace = "";
        switch (fieldType.charAt(0)) {
            case 'I' -> {
                replace = "int";
            }
            case 'Z' -> {
                replace = "boolean";
            }
            case 'B' -> {
                replace = "byte";
            }
            case 'C' -> {
                replace = "char";
            }
            case 'D' -> {
                replace = "double";
            }
            case 'F' -> {
                replace = "float";
            }
            case 'J' -> {
                replace = "long";
            }
            case 'S' -> {
                replace = "short";
            }
            case 'V' -> {
                replace = "void";
            }
        }

        return replace.isEmpty() ? fieldType : fieldType.length() > 1 ? replace + fieldType.substring(1) : replace;
    }

    public List<PrintAnnotationVisitor> getAnnotations() {
        return annotations;
    }

    public List<FieldInfo> getFieldInfoList() {
        return fieldInfoList;
    }

    public List<MethodInfo> getMethodInfoList() {
        return methodInfoList;
    }
}