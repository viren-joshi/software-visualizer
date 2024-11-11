package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.utils.ClassVisitor;
import com.g8.utils.FileProps;
import com.google.gson.Gson;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.objectweb.asm.ClassReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
public class DependencyHandler {

    // Stores the package name that has all the user classes
    private String USER_PACKAGE_PREFIX;

    // Collection of information of all the classes
    private List<ClassInfo> allClassInfoList;

    // To retrieve classInfo object by searching for its name
    private Map<String, ClassInfo> classInfoMap;

    // Stores nested class relationship
    private Map<String, List<String>> parentClassToNestedClassesMap;

    // JSON string containing internal dependencies. Used as a response to a user's request
    private String internalDep;

    // JSON string containing external dependencies. Used as a response to a user's request
    private String externalDep;

    private Gson gson;

    public DependencyHandler() {
        this.allClassInfoList = new ArrayList<>();
        this.classInfoMap = new HashMap<>();
        this.parentClassToNestedClassesMap = new HashMap<>();
        internalDep = null;
        gson = new Gson();
    }

    // Saves user uploaded JAR file in the system
    protected void saveFile(MultipartFile file, String jarFilePath) throws Exception {

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
    }

    // Extracts internal and external dependencies
    protected void analyzeFile(String jarFilePath) throws Exception {

        try (JarFile jarFile = new JarFile(jarFilePath)) {
            jarFile.stream()
                .forEach(entry -> {

                    // Filtering valid user defined classes to extract their dependencies
                    if (entry.getName().endsWith(".class") && entry.getName().contains(USER_PACKAGE_PREFIX)) {
                        try {
                            processClassEntry(jarFile, entry);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // POM file has external dependencies
                    if (entry.getName().endsWith("pom.xml")) {
                        try {
                            externalDep = analyzePomDependencies(entry, jarFile);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        }

        // Filling nested class information
        for(Map.Entry<String, List<String>> entry : parentClassToNestedClassesMap.entrySet()) {
            classInfoMap.get(entry.getKey()).setNestedClassesList(entry.getValue());
        }

        // Convert the entire list of ClassInfo objects to JSON
        internalDep = gson.toJson(allClassInfoList);
    }

    // Visiting a class and storing the retrieved information in the list
    void processClassEntry(JarFile jarFile, JarEntry entry) throws Exception {

        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            ClassReader classReader = new ClassReader(inputStream);
            ClassVisitor visitor = new ClassVisitor(parentClassToNestedClassesMap);
            classReader.accept(visitor, 0);

            ClassInfo classInfo = visitor.getClassInfo();
            classInfoMap.put(classInfo.getName(), classInfo);
            allClassInfoList.add(classInfo);
        }
    }

    // Gives response to a user's request
    public ResponseEntity<String> analyzeUploadedProject(MultipartFile file, String classContainer) throws Exception {

        USER_PACKAGE_PREFIX = classContainer.replace(".", "/");

        if (!file.getOriginalFilename().endsWith(".jar")) {
            return new ResponseEntity<>("Unsupported file", HttpStatus.BAD_REQUEST);
        }

        // Get current project directory
        String projectDir = System.getProperty("user.dir");

        // Use the original filename from the uploaded file
        String jarFilePath = projectDir + File.separator + file.getOriginalFilename();

        FileProps.setFilePath(jarFilePath);

        // Saving the file
        saveFile(file, jarFilePath);

        // Analyzing the file
        analyzeFile(jarFilePath);

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    // Analyzes in external dependencies
    public String analyzePomDependencies(JarEntry entry, JarFile jar) throws Exception {

        List<Map<String, String>> dependenciesList = new ArrayList<>();

        if (entry != null) {
            try (InputStream pomInputStream = jar.getInputStream(entry)) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(pomInputStream);

                // Extract dependencies
                for (Dependency dependency : model.getDependencies()) {

                    Map<String, String> dependencyMap = new HashMap<>();

                    dependencyMap.put("groupId", dependency.getGroupId());
                    dependencyMap.put("artifactId", dependency.getArtifactId());
                    dependencyMap.put("version", dependency.getVersion() != null ? dependency.getVersion() : "");
                    dependencyMap.put("scope", dependency.getScope() != null ? dependency.getScope() : "");

                    dependenciesList.add(dependencyMap);
                }
            }
        }

        return gson.toJson(dependenciesList);
    }

    public String getInternalDependencies() {
        return this.internalDep;
    }

    public String getClassList() {
        return this.classInfoMap.keySet().toString();
    }

    protected void setUSER_PACKAGE_PREFIX(String val) {
        this.USER_PACKAGE_PREFIX = val;
    }

    public String getExternalDependencies() {
        return this.externalDep;
    }
}
