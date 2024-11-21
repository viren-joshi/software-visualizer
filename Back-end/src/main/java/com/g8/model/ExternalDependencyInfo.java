package com.g8.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalDependencyInfo {

    private String artifactId;
    private String groupId;
    private String scope;
    private String version;
}
