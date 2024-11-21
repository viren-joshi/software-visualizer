package com.g8.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldInfo {

    private String identifier;
    private String datatype;
    private List<String> annotationList = new ArrayList<>();  // To store annotations
    private boolean isStatic;
    private boolean isAnnotated;

}

