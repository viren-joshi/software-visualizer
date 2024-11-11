package com.g8.model;

import java.util.ArrayList;
import java.util.List;

public class FieldInfo {
    private String identifier;
    private String datatype;
    private List<String> annotationList = new ArrayList<>();  // To store annotations
    private boolean isStatic;
    private boolean isAnnotated;

    public FieldInfo() {
    }

    public FieldInfo(String identifier, String datatype, List<String> annotationList, boolean isStatic, boolean isAnnotated) {
        this.identifier = identifier;
        this.datatype = datatype;
        this.annotationList = annotationList;
        this.isStatic = isStatic;
        this.isAnnotated = isAnnotated;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public List<String> getAnnotations() {   // Getter for annotations
        return annotationList;
    }

    public void setAnnotations(List<String> annotations) {   // Setter for annotations
        this.annotationList = annotations;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isAnnotated() {
        return isAnnotated;
    }

    public void setAnnotated(boolean isAnnotated) {
        this.isAnnotated = isAnnotated;
    }
}

