package com.g8.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Methods (Functions) of a Class.
public class ClassMethod {
    public String methodName;
    public List<String> annotations;
    public boolean isStatic;

    public ClassMethod() {
        methodName = "";
        annotations = new ArrayList<>();
    }
}
