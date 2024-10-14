package com.g8.service;

import com.g8.properties.FileProps;
import javassist.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@Service
public class DependecyHandler {

    // stores the package name that has all the user classes
    private static String USER_CLASS_CONTAINER;

    public String analyzeUploadedProject(MultipartFile file, String classContainer) throws Exception {

        USER_CLASS_CONTAINER = classContainer;
        StringBuilder result = new StringBuilder("Class Dependencies:\n");

        // Get current project directory
        String projectDir = System.getProperty("user.dir");
        String jarFilePath = projectDir + File.separator + file.getOriginalFilename(); // Use the original filename from the uploaded file
        FileProps.setFilePath(jarFilePath);

        // Save the uploaded JAR file to this project's folder
        try (InputStream fileInputStream = file.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(jarFilePath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Write the file content to the new JAR file
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // Accessing the jar file
        try (InputStream fileInputStream = new FileInputStream(FileProps.getFilePath());
             JarInputStream jarStream = new JarInputStream(fileInputStream)) {

            // Initialize ClassPool
            ClassPool pool = ClassPool.getDefault();
            // Add JAR file to ClassPool
            pool.appendClassPath(jarFilePath);
            JarEntry entry;

            while ((entry = jarStream.getNextJarEntry()) != null) {

                // [Debug] Checking if pom file exists in the jar file
                if(entry.getName().endsWith("pom.xml")) {
                    System.out.println("Found pom.xml file at" + entry.getName());
                }

                if (entry.getName().endsWith(".class")) {

                    // Transform the entry name to a fully qualified class name
                    String className = entry.getName().replace("/", ".").replace(".class", "");

                    // Filter out non-user classes
                    if (!className.startsWith(USER_CLASS_CONTAINER)) {
                        // Skip any class that are not inside the user-defined package
                        continue;
                    }

                    try {
                        System.out.println("Analyzing user class: " + className);

                        // Load the class using ClassPool
                        CtClass ctClass = pool.getCtClass(className);
                        analyzeClassDependencies(ctClass, result);
                    } catch (javassist.NotFoundException e) {
                        // Handle case where class cannot be found
                        System.err.println("Class not found in ClassPool: " + className);
                        e.printStackTrace();
                    } catch (Exception e) {
                        // Log other exceptions for better understanding
                        System.err.println("Failed to analyze class: " + className + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error while analyzing project: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return result.toString();
    }


    // analyzes internal dependencies
    private void analyzeClassDependencies(CtClass ctClass, StringBuilder result) throws Exception {

        // Check for superclass inheritance
        CtClass superclass = ctClass.getSuperclass();
        if (superclass != null && superclass.getName().startsWith(USER_CLASS_CONTAINER)) {
            result.append(ctClass.getName()).append(" inherits from ").append(superclass.getName()).append("\n");
        }

        // Check implemented interfaces
        for (CtClass iface : ctClass.getInterfaces()) {
            if (iface.getName().startsWith(USER_CLASS_CONTAINER)) {
                result.append(ctClass.getName()).append(" implements ").append(iface.getName()).append("\n");
            }
        }

        // Inspect methods
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            result.append(ctClass.getName()).append(" has method ").append(method.getName()).append("\n");
        }

        for (javassist.CtField field : ctClass.getDeclaredFields()) {
            String fieldType = field.getType().getName();
            // Check if the field type belongs to the user-defined package
            if (fieldType.startsWith(USER_CLASS_CONTAINER)) {
                result.append(ctClass.getName()).append(" has a composition with ").append(fieldType).append(" (field: ").append(field.getName()).append(")\n");
            }
        }

        // Inspect field annotations
        for (javassist.CtField field : ctClass.getDeclaredFields()) {
            Object[] annotations = field.getAnnotations();
            for (Object annotation : annotations) {
                result.append(ctClass.getName())
                        .append(" has field ")
                        .append(field.getName())
                        .append(" annotated with ")
                        .append(annotation.toString())
                        .append("\n");
            }
        }

        // Inspect method annotations
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            Object[] methodAnnotations = method.getAnnotations();
            for (Object annotation : methodAnnotations) {
                result.append(ctClass.getName())
                        .append(" has method ")
                        .append(method.getName())
                        .append(" annotated with ")
                        .append(annotation.toString())
                        .append("\n");
            }
        }

        Object[] classAnnotations = ctClass.getAnnotations();
        for (Object annotation : classAnnotations) {
            result.append(ctClass.getName())
                    .append(" is annotated with ")
                    .append(annotation.toString())
                    .append("\n");
        }

        // Inspect inner classes
        for (CtClass innerClass : ctClass.getDeclaredClasses()) {
            result.append(ctClass.getName())
                    .append(" contains inner class ")
                    .append(innerClass.getName())
                    .append("\n");
        }

        // Inspect static fields and methods
        for (CtField field : ctClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                result.append(ctClass.getName())
                        .append(" has static field ")
                        .append(field.getName())
                        .append(" of type ")
                        .append(field.getType().getName())
                        .append("\n");
            }
        }

        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                result.append(ctClass.getName())
                        .append(" has static method ")
                        .append(method.getName())
                        .append("\n");
            }
        }
    }
}
