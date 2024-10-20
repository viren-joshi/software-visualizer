package com.g8.service;
import com.g8.model.*;
import com.g8.properties.FileProps;
import javassist.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

@Service
public class DependencyHandler {

    // stores the package name that has all the user classes
    private static String USER_CLASS_CONTAINER;

    public String analyzeUploadedProject(MultipartFile file, String classContainer) throws Exception {

        USER_CLASS_CONTAINER = classContainer;

        // Get current project directory
        String projectDir = System.getProperty("user.dir");
        String jarFilePath = projectDir + File.separator + file.getOriginalFilename(); // Use the original filename from the uploaded file
        FileProps.setFilePath(jarFilePath);
        UserProject userProject = new UserProject();

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

            // Creating a UserProject Object


            // Initialize ClassPool
            ClassPool pool = ClassPool.getDefault();
            // Add JAR file to ClassPool
            pool.appendClassPath(jarFilePath);
            JarEntry entry;

            while ((entry = jarStream.getNextJarEntry()) != null) {

                // [Debug] Checking if pom file exists in the jar file.
                if(entry.getName().endsWith("pom.xml")) {
                    System.out.println("Found pom.xml file at " + entry.getName());
                }

                // Run for each class.
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
                        UserClass userClass = analyzeClassDependencies(ctClass);
                        userProject.userClassList.add(userClass);

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
            System.err.println("Error while analyzing project: ");
            e.printStackTrace();
            throw e;
        }

        // extract external dependency
//        extractExternalDependencies(FileProps.getFilePath(), result);

        Gson gson = new Gson();
        return gson.toJson(userProject);
    }

    // Extract external dependencies
    public static void extractExternalDependencies(String jarFilePath, StringBuilder sb) {

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
    private UserClass analyzeClassDependencies(CtClass ctClass) throws Exception{
        UserClass userClass = getUserClass(ctClass);
        userClass.name = ctClass.getName();

        userClass.classType = getClassType(ctClass);

        // Extracting Class Annotations.
        extractClassAnnotations(ctClass, userClass);

        // Extract inheritance dependencies
        extractInheritance(ctClass, userClass);

        // Extract implementation dependencies
        extractImplementation(ctClass, userClass);

        // Extract Methods
        extractMethods(ctClass, userClass);

        // Extract Variables
        extractVariables(ctClass, userClass);

        // Extract inner classes
        //extractInnerClasses(ctClass, userClass);

        return userClass;
    }

    private UserClass getUserClass(CtClass ctClass) throws ClassNotFoundException {
        // Check if the class is a Controller Class.
        Object[] classAnnotations = ctClass.getAnnotations();
        for (Object annotation : classAnnotations) {
            String annotationName = annotation.toString();
            if (annotationName.contains("PostMapping")) {
                UserControllerClass userControllerClass = new UserControllerClass();
                userControllerClass.isControllerClass = true;
                return new UserControllerClass();
            }
        }
        return new UserClass();
    }

    private ClassType getClassType (CtClass ctClass) {
        if(ctClass.isInterface()) {
            return ClassType.interfaceClass;
        } else if (Modifier.isAbstract(ctClass.getModifiers())) {
            return  ClassType.abstractClass;
        }
        return ClassType.normalClass;
    }

    private void extractClassAnnotations(CtClass ctClass, UserClass userClass) throws ClassNotFoundException {

        Object[] classAnnotations = ctClass.getAnnotations();
        for (Object annotation : classAnnotations) {
            if(userClass instanceof UserControllerClass) {
                String annotationName = annotation.toString();
                if (annotationName.contains("PostMapping")) {
                    ((UserControllerClass) userClass).requestMapping = getEndpointFromAnnotation(annotationName);
                    continue;
                }
            }
            userClass.annotations.add(annotation.toString());
        }
    }

    private void extractInheritance(CtClass ctClass, UserClass userClass) throws NotFoundException {

        CtClass superclass = ctClass.getSuperclass();
        if (superclass != null && superclass.getName().startsWith(USER_CLASS_CONTAINER)) {
            userClass.inherits = superclass.getName();
        }
    }

    private void extractImplementation(CtClass ctClass, UserClass result) throws NotFoundException {

        for (CtClass iClass : ctClass.getInterfaces()) {
            if (iClass.getName().startsWith(USER_CLASS_CONTAINER)) {
                result.implementationList.add(iClass.getName());
            }
        }
    }

    private void extractMethods(CtClass ctClass, UserClass userClass) throws ClassNotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            ClassMethod classMethod;
            classMethod = new ClassMethod();
            // Getting method's name.
            classMethod.methodName = method.getName();

            // Checking if the method is static.
            classMethod.isStatic = Modifier.isStatic(method.getModifiers());

            // Getting method annotations.
            Object[] methodAnnotations = method.getAnnotations();
            for(Object annotation: methodAnnotations) {
                String annotationName = annotation.toString();
                if (userClass instanceof UserControllerClass && annotationName.contains("PostMapping")) {
                    ((UserControllerClass) userClass).endpoints.add(getEndpointFromAnnotation(annotationName));
                    continue;
                }
                classMethod.annotations.add(annotation.toString());
            }
            userClass.methodList.add(classMethod);
        }
    }

//    private void extractInnerClasses(CtClass ctClass, StringBuilder result) throws NotFoundException {
//        for (CtClass innerClass : ctClass.getDeclaredClasses()) {
//            result.append(ctClass.getName())
//                    .append(" contains inner class ")
//                    .append(innerClass.getName())
//                    .append("\n");
//        }
//    }


    private void extractVariables(CtClass ctClass, UserClass userClass) throws NotFoundException, ClassNotFoundException {
        for(CtField field : ctClass.getDeclaredFields()) {
            ClassVariable classVariable = new ClassVariable();
            classVariable.datatype = field.getType().getName();
            classVariable.identifier = field.getName();
            classVariable.isStatic = Modifier.isStatic(field.getModifiers());

            Object[] annotations = field.getAnnotations();
            classVariable.isAnnotated = annotations.length != 0;
            for(Object annotation: field.getAnnotations()) {
                classVariable.annotationList.add(annotation.toString());
            }

            userClass.variableList.add(classVariable);
        }
    }



    private String getEndpointFromAnnotation(String annotation) {
        Pattern pattern = Pattern.compile("value=\\{?\"?([^\"]+)\"?}?"); // Matches the value
        Matcher matcher = pattern.matcher(annotation);
        if(matcher.find()) {
            return matcher.group(1);
        }
        // In-case the regex doesn't work.
        return "/";
    }
}
