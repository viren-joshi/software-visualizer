package com.g8.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
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

}