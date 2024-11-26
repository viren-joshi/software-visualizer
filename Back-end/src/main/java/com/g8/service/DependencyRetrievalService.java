package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.model.ExternalDependencyInfo;
import com.google.cloud.firestore.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DependencyRetrievalService {

    private final CollectionReference collectionReference;
    private final CollectionReference userProjectsCollection;
    private static final Gson gson = new Gson();

    DependencyRetrievalService(Firestore firestore) {
        this.collectionReference = firestore.collection("projects");
        this.userProjectsCollection = firestore.collection("user_projects");
    }

    @Async
    public CompletableFuture<String> getInternalDependencies(String projectId) {

        try {
            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = collectionReference.document(projectId).get().get();

            List<Map<String, Object>> projects = (List<Map<String, Object>>) projectCollection.get("intDep");

            if (projects != null && !projects.isEmpty()) {

                Type type = new TypeToken<List<ClassInfo>>() {}.getType();

                List<ClassInfo> dependencies = gson.fromJson(gson.toJson(projects), type);

                // Convert the list of ClassInfo objects to JSON
                String jsonOutput = gson.toJson(dependencies);
                return CompletableFuture.completedFuture(jsonOutput);
            }

            return CompletableFuture.completedFuture(null);

        }  catch (Exception e) {
            throw new RuntimeException("Error retrieving internal dependencies: " + e.getMessage());
        }
    }
    
    @Async
    public CompletableFuture<String> getClassList(String projectId) {

        try {

            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = collectionReference.document(projectId).get().get();

            List<String> classList = (List<String>) projectCollection.get("classList");

            if (classList != null && !classList.isEmpty()) {
                return CompletableFuture.completedFuture(gson.toJson(classList));
            }
            return CompletableFuture.completedFuture(null);

        }  catch (Exception e) {
            throw new RuntimeException("Error retrieving classList: " + e.getMessage());
        }
    }

    @Async
    public CompletableFuture<String> getExternalDependencies(String projectId) {

        try {
            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = collectionReference.document(projectId).get().get();

            List<Map<String, Object>> projects = (List<Map<String, Object>>) projectCollection.get("extDep");

            if (projects != null && !projects.isEmpty()) {

                Type type = new TypeToken<List<ExternalDependencyInfo>>() {}.getType();

                List<ExternalDependencyInfo> dependencies = gson.fromJson(gson.toJson(projects), type);

                // Convert the list of ClassInfo objects to JSON
                return CompletableFuture.completedFuture(gson.toJson(dependencies));
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving external dependencies: " + e.getMessage());
        }
    }

    @Async
    public CompletableFuture<String> saveData(List<Map<String, Object>> internalDependencies, List<Map<String, String>> externalDependencies, List<String> classList) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = collectionReference.document();

        // Prepare data for both internal and external dependencies
        Map<String, Object> dependenciesData = new HashMap<>();
        dependenciesData.put("intDep", internalDependencies);
        dependenciesData.put("extDep", externalDependencies);
        dependenciesData.put("classList", classList);

        // Save the data into the Firestore document
        documentReference.set(dependenciesData).get();

        // Retrieve the generated document ID
        return CompletableFuture.completedFuture(documentReference.getId());
    }

    @Async
    public CompletableFuture<Void> saveProjectToUser(String projectId, String userId) {
        DocumentReference userDocRef = userProjectsCollection.document(userId);
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("projectId", projectId);
        projectInfo.put("custom_view", "");

        return CompletableFuture.runAsync(() -> {
            try {
                // Get the document snapshot
                DocumentSnapshot snapshot = userDocRef.get().get();
    
                if (snapshot.exists()) {
                    // Document exists, update the projects array
                    userDocRef.update("projects", FieldValue.arrayUnion(projectInfo)).get();
                } else {
                    // Document does not exist, create a new one
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("projects", Collections.singletonList(projectInfo));
                    userDocRef.set(userData, SetOptions.merge()).get();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error saving project to user: " + e.getMessage(), e);
            }
        });

    }

}