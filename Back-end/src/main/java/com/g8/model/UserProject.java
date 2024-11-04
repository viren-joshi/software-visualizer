package com.g8.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProject {
    private String classContainer;
    private List<UserClass> userClassList;
    private List<ExternalDependency> externalDependencyList;

    public UserProject () {
        classContainer = "";
        userClassList = new ArrayList<>();
        externalDependencyList = new ArrayList<>();
    }

    public String getClassContainer() {
        return classContainer;
    }

    public void setClassContainer(String classContainer) {
        this.classContainer = classContainer;
    }

    public List<UserClass> getUserClassList() {
        return userClassList;
    }

    public void setUserClassList(List<UserClass> userClassList) {
        this.userClassList = userClassList;
    }

    public List<ExternalDependency> getExternalDependencyList() {
        return externalDependencyList;
    }

    public void setExternalDependencyList(List<ExternalDependency> externalDependencyList) {
        this.externalDependencyList = externalDependencyList;
    }
}
