package com.g8.utils;

import com.g8.model.ClassInfo;
import com.g8.model.FieldInfo;
import com.g8.model.MethodInfo;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;

public class ClassVisitor extends org.objectweb.asm.ClassVisitor {

    // Stores class information
    private ClassInfo classInfo;

    // Stores method information
    private List<MethodInfo> methodInfoList;

    // Stores variable information
    private List<FieldInfo> fieldInfoList;

    // Preserves nested class relationship
    Map<String, List<String>> parentClassToNestedClassesMap;

    // Stores the annotations information so that it can be added to the classInfo object once the class has been visited
    private final List<PrintAnnotationVisitor> annotations;

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

        // get the final classname that might contain nested class name too
        String combinedName = nameArr[nameArr.length - 1];

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

        // If the super class is not Object class, set the inheritance dependency
        classInfo.setInherits("java/lang/Object".equals(superName) ? "" : superName.replace('/', '.'));
        if(interfaces != null) {
            List<String> updatedInterfaces = Arrays.stream(interfaces)
                    .map(interfaceName -> interfaceName.replace('/', '.'))
                    .collect(Collectors.toList());
            classInfo.setImplementationList(updatedInterfaces);
        }

        // Setting the class type
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
                    .add(name);

            classInfo.setNested(true);
        }
    }

    // Visits methods in a class
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        // Skip the 'init' method (constructor) by checking the method name
        if ("<init>".equals(name) || name.startsWith("lambda$")) {
            return null;
        }

        MethodInfo currentMethod = new MethodInfo();
        currentMethod.setMethodName(name);
        currentMethod.setStatic((access & Opcodes.ACC_STATIC) != 0); // Check if the method is static

        // Add method to the methodList
        methodInfoList.add(currentMethod);
        return new MethodAnnotationVisitor(currentMethod);
    }

    // Visiting variables in a class
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        // Create FieldInfo object for this field
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.setIdentifier(name);

        // Extract datatype from the descriptor (e.g., "Ljava/lang/String;" becomes "java.lang.String", "I" becomes "integer)
        String fieldType = getFieldType(descriptor);

        fieldInfo.setDatatype(fieldType);

        // Determine if the field is static
        fieldInfo.setStatic((access & Opcodes.ACC_STATIC) != 0);

        // Add the field info to the list
        fieldInfoList.add(fieldInfo);

        return new FieldAnnotationVisitor(fieldInfo);  // Return a visitor to collect annotations
    }


    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {

        // making the format as closely equivalent to the real life format that a user sees
        String annotationName = descriptor.replace('/', '.').replace(";", "");

        // removing the L from the beginning and adding @
        annotationName = "@" + annotationName.substring(1);

        String[] ann = annotationName.split("\\.");

        // Checking if the class is a controller class
        if(ann[ann.length - 1].toLowerCase().contains("controller"))
            classInfo.setControllerClass(true);

        PrintAnnotationVisitor annotationVisitor = new PrintAnnotationVisitor(annotationName);
        annotations.add(annotationVisitor);
        return annotationVisitor;
    }

    public ClassInfo getClassInfo() {
        classInfo.setVariableList(fieldInfoList);
        classInfo.setMethodList(methodInfoList);
        return classInfo;
    }

    // Adds all the annotations from the list to the classInfo object at the end of the visit
    // Required to do that because the test was failing otherwise
    @Override
    public void visitEnd() {

        for (PrintAnnotationVisitor annotationVisitor : annotations) {
            String fullAnnotation = annotationVisitor.getFullAnnotation();
            classInfo.getAnnotations().add(fullAnnotation);
        }
        annotations.clear();  // Reset list for the next visit
    }

    // Getting the field type in a proper format as the raw output is not understandable
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


        String replace = extractType(fieldType);
        // Check if 'replace' is empty
        boolean isReplaceEmpty = replace.isEmpty();

        // Check if 'fieldType' has more than one character
        boolean isFieldTypeLong = fieldType.length() > 1;

        // Compute the result based on conditions
        String resultWhenReplaceNotEmpty = isFieldTypeLong ? replace + fieldType.substring(1) : replace;

        // Final result
        return isReplaceEmpty ? fieldType : resultWhenReplaceNotEmpty;
    }

    private String extractType(String fieldType) {

        String replace;

        switch (fieldType.charAt(0)) {
            case 'I' -> replace = "int";
            case 'Z' -> replace = "boolean";
            case 'B' -> replace = "byte";
            case 'C' -> replace = "char";
            case 'D' -> replace = "double";
            case 'F' -> replace = "float";
            case 'J' -> replace = "long";
            case 'S' -> replace = "short";
            default -> replace = "";
        }

        return replace;
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