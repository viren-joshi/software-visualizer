package com.g8.model;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {

    private String methodName;
    private List<String> annotations = new ArrayList<>();
    private boolean isStatic;

    public MethodInfo() {
    }

    public MethodInfo(String methodName, List<String> annotations, boolean isStatic) {
        this.methodName = methodName;
        this.annotations = annotations;
        this.isStatic = isStatic;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}

