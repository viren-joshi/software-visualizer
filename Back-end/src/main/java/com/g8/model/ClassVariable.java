package com.g8.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassVariable {
    public String identifier, datatype;
    public List<String> annotationList;
    public boolean isStatic, isAnnotated;

    public ClassVariable() {
        identifier = "";
        datatype = "";
        annotationList = new ArrayList<>();
        isStatic = false;
        isAnnotated = false;
    }
}
