package com.g8.service;

import com.g8.model.ClassInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    }

    protected void analyzeFile(String jarFilePath) throws Exception {

    }

    void processClassEntry(JarFile jarFile, JarEntry entry) throws Exception {

    }

    public String analyzeUploadedProject(MultipartFile file, String classContainer) throws Exception {
        return "Project uploaded and analyzed successfully";
    }

    public String getInternalDependencies() {
        return "";
    }

    public String getClassList() {
        return "";
    }

    protected void setUSER_PACKAGE_PREFIX(String val) {
    }

}
