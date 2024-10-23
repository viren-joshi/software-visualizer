package com.g8.model;

import lombok.Getter;

public class ExternalDependency {
    public String groupId, artifactId, version, scope;

    public ExternalDependency () {
        this.groupId = "";
        this.artifactId = "";
        this.version = "";
        this.scope = "";
    }
}

