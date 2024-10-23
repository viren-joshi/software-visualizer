package com.g8.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserClass {
    public String name, inherits;
    public ClassType classType;
    public List<ClassVariable> variableList;
    public List<ClassMethod> methodList;
    public boolean isNested, isControllerClass;
    public List<UserClass> nestedClassesList;
    public List<String> annotations;
    public List<String> implementationList;

    public UserClass() {
        name = "";
        inherits = "";
        variableList = new ArrayList<>();
        methodList = new ArrayList<>();
        isNested = false;
        isControllerClass = false;
        nestedClassesList = new ArrayList<>();
        annotations = new ArrayList<>();
        implementationList = new ArrayList<>();
    }
}
