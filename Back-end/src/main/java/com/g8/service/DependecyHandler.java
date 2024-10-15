package com.g8.service;

import com.g8.properties.FileProps;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
                    System.out.println("Found pom.xml file at " + entry.getName());
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

        // extract external dependency
        extractDependenciesFromJar(FileProps.getFilePath(), result);

        return result.toString();
    }

    // Extract external dependencies
    public static void extractDependenciesFromJar(String jarFilePath, StringBuilder sb) {

        JarEntry pomEntry = null;

        try (JarFile jarFile = new JarFile(jarFilePath)) {
            // Find all pom.xml entries in the JAR
            Iterator<JarEntry> entries = (Iterator<JarEntry>) jarFile.entries();
            while (entries.hasNext()) {
                JarEntry entry = entries.next();
                if (entry.getName().equals("pom.xml")) {
                   pomEntry = entry;
                    break;
                }
            }

            // If there are multiple pom.xml files, print their paths
            if (pomEntry == null) {
                System.out.println("No pom.xml files found in the JAR.");
                return;
            }

            JarEntry selectedPomEntry = pomEntry;
            System.out.println("Selected pom.xml: " + selectedPomEntry.getName());

            // Parse the selected pom.xml
            try (InputStream input = jarFile.getInputStream(selectedPomEntry)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(input);
                document.getDocumentElement().normalize();

                // Extract dependencies
                NodeList dependencyNodes = document.getElementsByTagNameNS("http://maven.apache.org/POM/4.0.0","dependency");
                for (int i = 0; i < dependencyNodes.getLength(); i++) {
                    String groupId = dependencyNodes.item(i).getChildNodes().item(1).getTextContent();
                    String artifactId = dependencyNodes.item(i).getChildNodes().item(3).getTextContent();
//                    String version = dependencyNodes.item(i).getChildNodes().item(5).getTextContent();
                    sb.append("Dependency: ").append(groupId).append(", Artifact ID: ").append(artifactId).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }
}
