package com.g8.model;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private String name;
    private String inherits;
    private String classType;
    private List<FieldInfo> variableList = new ArrayList<>();
    private List<MethodInfo> methodList = new ArrayList<>();
    private boolean isNested = false;
    private boolean isControllerClass = false;
    private List<String> nestedClassesList = new ArrayList<>();
    private List<String> annotations = new ArrayList<>();
    private List<String> implementationList = new ArrayList<>();

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInherits() {
        return inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = inherits;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public List<FieldInfo> getVariableList() {
        return variableList;
    }

    public void setVariableList(List<FieldInfo> variableList) {
        this.variableList = variableList;
    }

    public List<MethodInfo> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<MethodInfo> methodList) {
        this.methodList = methodList;
    }

    public boolean isIsNested() {
        return isNested;
    }

    public void setIsNested(boolean isNested) {
        this.isNested = isNested;
    }

    public boolean isIsControllerClass() {
        return isControllerClass;
    }

    public void setIsControllerClass(boolean isControllerClass) {
        this.isControllerClass = isControllerClass;
    }

    public List<String> getNestedClassesList() {
        return nestedClassesList;
    }

    public void setNestedClassesList(List<String> nestedClassesList) {
        this.nestedClassesList = nestedClassesList;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<String> getImplementationList() {
        return implementationList;
    }

    public void setImplementationList(List<String> implementationList) {
        this.implementationList = implementationList;
    }
}
