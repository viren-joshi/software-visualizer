package com.g8.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodInfo {

    private String methodName;
    private List<String> annotations = new ArrayList<>();
    private boolean isStatic;

}

