package com.g8.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserControllerClass extends UserClass {
    public String requestMapping;
    public List<String> endpoints;

    public UserControllerClass() {
        requestMapping = "";
        endpoints = new ArrayList<>();
    }
}
