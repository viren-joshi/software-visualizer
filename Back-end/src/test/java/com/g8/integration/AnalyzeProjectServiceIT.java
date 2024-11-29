package com.g8.integration;

import com.g8.service.AnalyzeProjectService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AnalyzeProjectServiceIT {

   private AnalyzeProjectService analyzeProjectService;
   private String filePath = "src/test/resources/blog-0.0.1-SNAPSHOT.jar";
   private String userId = "abc";

    @Autowired
    public AnalyzeProjectServiceIT(AnalyzeProjectService analyzeProjectService) {
       this.analyzeProjectService = analyzeProjectService;
   }

    @BeforeEach
    public void confiuguration() {
       analyzeProjectService.setUSER_PACKAGE_PREFIX("com/blog");
   }

    @Test
    public void analyzeFileTest() throws Exception {

       String projectId = analyzeProjectService.analyzeFile(filePath, userId);
       Assertions.assertNotNull(projectId);
   }
}