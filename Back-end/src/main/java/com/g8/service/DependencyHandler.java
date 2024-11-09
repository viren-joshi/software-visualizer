package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.utils.AnnotationClassVisitor;
import com.google.gson.Gson;
import org.objectweb.asm.ClassReader;
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

    // stores the package name that has all the user classes
    private String USER_PACKAGE_PREFIX;
    private List<ClassInfo> allClassInfoList;
    private Map<String, ClassInfo> classInfoMap;
    private Map<String, List<String>> parentClassToNestedClassesMap;
    private String internalDep;

    public DependencyHandler() {
        this.allClassInfoList = new ArrayList<>();
        this.classInfoMap = new HashMap<>();
        this.parentClassToNestedClassesMap = new HashMap<>();
        internalDep = null;
    }

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

    protected void analyzeFile(String jarFilePath) throws Exception {

        try (JarFile jarFile = new JarFile(jarFilePath)) {
            jarFile.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .filter(entry -> entry.getName().contains(USER_PACKAGE_PREFIX))// Filter before processing
                    .forEach(entry -> {
                        System.out.println(entry.getName());
                        try {
                            processClassEntry(jarFile, entry);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        for(Map.Entry<String, List<String>> entry : parentClassToNestedClassesMap.entrySet()) {
            classInfoMap.get(entry.getKey()).setNestedClassesList(entry.getValue());
        }

        Gson gson = new Gson();
        internalDep = gson.toJson(allClassInfoList);  // Convert the entire list of ClassInfo objects to JSON
    }

    void processClassEntry(JarFile jarFile, JarEntry entry) throws Exception {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            ClassReader classReader = new ClassReader(inputStream);
            AnnotationClassVisitor visitor = new AnnotationClassVisitor(parentClassToNestedClassesMap);
            classReader.accept(visitor, 0);

            // After processing, convert to JSON using Gson
            ClassInfo classInfo = visitor.getClassInfo();
            classInfoMap.put(classInfo.getName(), classInfo);
            allClassInfoList.add(classInfo);
        }
    }

    public String analyzeUploadedProject(MultipartFile file, String classContainer) throws Exception {

        USER_PACKAGE_PREFIX = classContainer.replace(".", "/");

        if (!file.getOriginalFilename().endsWith(".jar")) {
            return "The uploaded file is not a JAR file. Please upload a valid JAR file.";
        }

        // Get current project directory
        String projectDir = System.getProperty("user.dir");

        // Use the original filename from the uploaded file
        String jarFilePath = projectDir + File.separator + file.getOriginalFilename();

        saveFile(file, jarFilePath);

        analyzeFile(jarFilePath);

        return "Project uploaded and analyzed successfully";
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


}
