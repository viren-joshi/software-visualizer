package com.g8.integration;

import com.g8.service.DependencyRetrievalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class DependencyRetrievalServiceIT {

    private final DependencyRetrievalService dependencyRetrievalService;

    // sample project ID
    private final String projectId = "KfSwfcW2AI6BqZzsxPPT";

    @Autowired
    public DependencyRetrievalServiceIT(DependencyRetrievalService dependencyRetrievalService) {
        this.dependencyRetrievalService = dependencyRetrievalService;
    }

    @Test
    public void testInternalDependencyRetrieval() throws Exception {

        CompletableFuture<String> resposne = dependencyRetrievalService.getInternalDependencies(projectId);

        assertNotNull(resposne.get());
    }

    @Test
    public void testExternalDependencyRetrieval() throws Exception {

        CompletableFuture<String> resposne = dependencyRetrievalService.getExternalDependencies(projectId);

        assertNotNull(resposne.get());
    }

    @Test
    public void testClassListRetrieval() throws Exception {

        CompletableFuture<String> resposne = dependencyRetrievalService.getClassList(projectId);

        assertNotNull(resposne.get());
    }
}
