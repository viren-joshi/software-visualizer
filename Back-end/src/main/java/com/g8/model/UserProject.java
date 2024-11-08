package com.g8.model;

import java.util.ArrayList;
import java.util.List;

public class UserProject {
    private String classContainer;
    private List<UserClass> internalDependencyList;
    private List<ExternalDependency> externalDependencyList;
    private List<String> classNames;

    public UserProject () {
        classContainer = "";
        internalDependencyList = new ArrayList<>();
        externalDependencyList = new ArrayList<>();
        classNames = new ArrayList<>();
    }

    public String getClassContainer() {
        return classContainer;
    }

    public void setClassContainer(String classContainer) {
        this.classContainer = classContainer;
    }

    public List<UserClass> getInternalDependencyList() {
        return internalDependencyList;
    }

    public void setInternalDependencyList(List<UserClass> internalDependencyList) {
        this.internalDependencyList = internalDependencyList;
    }

    public List<ExternalDependency> getExternalDependencyList() {
        return externalDependencyList;
    }

    public void setExternalDependencyList(List<ExternalDependency> externalDependencyList) {
        this.externalDependencyList = externalDependencyList;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }
}
