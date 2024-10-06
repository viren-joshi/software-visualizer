package com.g8.service;

import com.g8.properties.FileProps;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@Service
public class DependecyHandler {

    private static String USER_CLASS_CONTAINER; // Change this to the package prefix of your user's classes

    public String analyzeUploadedProject(MultipartFile file, String classContainer) throws Exception {

        StringBuilder result = new StringBuilder("Class Dependencies:\n");
        USER_CLASS_CONTAINER = classContainer;

        String projectDir = System.getProperty("user.dir"); // Get current project directory
        String jarFilePath = projectDir + File.separator + file.getOriginalFilename(); // Use the original filename from the uploaded file
        FileProps.setFilePath(jarFilePath);

        // Save the uploaded JAR file to the specified path
        try (InputStream fileInputStream = file.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(jarFilePath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Write the file content to the new JAR file
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // Use InputStream directly from MultipartFile
        try (InputStream fileInputStream = file.getInputStream();
             JarInputStream jarStream = new JarInputStream(fileInputStream)) {

            // Initialize ClassPool and add the JAR file path to the ClassPool
            ClassPool pool = ClassPool.getDefault();
//            System.out.println(pool.toString());
//            String jarFilePath = "jar:file:" + file.getOriginalFilename() + "!/"; // Create JAR file path for ClassPool
            pool.appendClassPath(jarFilePath); // Add JAR file to ClassPool
//            System.out.println("==== pool");
//            System.out.println(pool.toString());
            JarEntry entry;

            while ((entry = jarStream.getNextJarEntry()) != null) {

                if(entry.getName().endsWith("pom.xml")) {
                    System.out.println("Found pom.xml file at" + entry.getName());
                }

                if (entry.getName().endsWith(".class")) {
                    // Transform the entry name to a fully qualified class name
//                    System.out.println(entry.getName());
                    String className = entry.getName().replace("/", ".").replace(".class", "");

                    // Filter out non-user classes
                    if (!className.startsWith(USER_CLASS_CONTAINER)) {
                        continue; // Skip any class that does not belong to the user-defined package
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
    }
}
