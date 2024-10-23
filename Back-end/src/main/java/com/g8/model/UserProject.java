package com.g8.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProject {
    public String classContainer;
    public List<UserClass> userClassList;
    public List<ExternalDependency> externalDependencyList;

    public UserProject () {
        classContainer = "";
        userClassList = new ArrayList<>();
        externalDependencyList = new ArrayList<>();
    }
}
