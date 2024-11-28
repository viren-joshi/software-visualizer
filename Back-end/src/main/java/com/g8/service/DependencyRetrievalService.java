package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.model.ExternalDependencyInfo;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DependencyRetrievalService {

    private final CollectionReference projectCollectionReference;
    private final CollectionReference userProjectsCollection;
    private final CollectionReference customViewsCollection;
    private final Logger logger;
    private static final Gson gson = new Gson();

    public DependencyRetrievalService(Firestore firestore) {
        this.projectCollectionReference = firestore.collection("projects");
        this.userProjectsCollection = firestore.collection("user_projects");
        this.customViewsCollection = firestore.collection("custom_views");
        logger = LoggerFactory.getLogger(DependencyRetrievalService.class);
    }

    @Async
    public CompletableFuture<String> getInternalDependencies(String projectId) {

        try {

            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = projectCollectionReference.document(projectId).get().get();

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
            logger.info(e.getMessage());
            throw new RuntimeException("Error retrieving internal dependencies");
        }
    }
    
    @Async
    public CompletableFuture<String> getClassList(String projectId) {

        try {

            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = projectCollectionReference.document(projectId).get().get();

            List<String> classList = (List<String>) projectCollection.get("classList");

            if (classList != null && !classList.isEmpty()) {
                return CompletableFuture.completedFuture(gson.toJson(classList));
            }
            return CompletableFuture.completedFuture(null);

        }  catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Error retrieving classList");
        }
    }

    @Async
    public CompletableFuture<String> getExternalDependencies(String projectId) {

        try {
            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = projectCollectionReference.document(projectId).get().get();

            List<Map<String, Object>> projects = (List<Map<String, Object>>) projectCollection.get("extDep");

            if (projects != null && !projects.isEmpty()) {

                Type type = new TypeToken<List<ExternalDependencyInfo>>() {}.getType();

                List<ExternalDependencyInfo> dependencies = gson.fromJson(gson.toJson(projects), type);

                // Convert the list of ClassInfo objects to JSON
                return CompletableFuture.completedFuture(gson.toJson(dependencies));
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Error retrieving external dependencies");
        }
    }

    @Async
    public CompletableFuture<String> saveData(List<Map<String, Object>> internalDependencies, List<Map<String, String>> externalDependencies, List<String> classList) {

        try {
            DocumentReference documentReference = projectCollectionReference.document();

            // Prepare data for both internal and external dependencies
            Map<String, Object> dependenciesData = new HashMap<>();
            dependenciesData.put("intDep", internalDependencies);
            dependenciesData.put("extDep", externalDependencies);
            dependenciesData.put("classList", classList);

            // Save the data into the Firestore document
            documentReference.set(dependenciesData).get();

            // Retrieve the generated document ID
            return CompletableFuture.completedFuture(documentReference.getId());
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Error while saving the project data in firestore");
        }
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
                logger.info(e.getMessage());
                throw new RuntimeException("Error while saving project to user");
            }
        });

    }

    @Async
    public CompletableFuture<List<Map<String, Object>>> getUserProjects(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentSnapshot snapshot = userProjectsCollection.document(userId).get().get();
                if (snapshot.exists()) {
                    List<Map<String, Object>> projects = (List<Map<String, Object>>) snapshot.get("projects");
                    return projects != null ? projects : Collections.emptyList();
                }
                return Collections.emptyList();
            } catch (Exception e) {
                logger.info(e.getMessage());
                throw new RuntimeException("Error retrieving user projects: " + e.getMessage(), e);
            }
        });
    }

    @Async
    public CompletableFuture<String> createCustomView(String userId, String projectId, Map<String, Object> data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create a new custom view document
                DocumentReference customViewDocRef = customViewsCollection.document();
                Map<String, Object> customViewData = new HashMap<>();
                customViewData.put("data", data);

                customViewDocRef.set(customViewData).get(); // Synchronous set operation to ensure completion

                // Update the user document with the custom view ID
                DocumentReference userDocRef = userProjectsCollection.document(userId);
                ApiFuture<DocumentSnapshot> userDocFuture = userDocRef.get();
                DocumentSnapshot userDocSnapshot = userDocFuture.get();

                if (userDocSnapshot.exists()) {
                    // Retrieve the list of projects
                    List<Map<String, Object>> projects = (List<Map<String, Object>>) userDocSnapshot.get("projects");
                    if (projects != null) {
                        // Update the custom_view field of the matching project
                        for (Map<String, Object> project : projects) {
                            if (projectId.equals(project.get("projectId"))) {
                                project.put("custom_view", customViewDocRef.getId());
                                break;
                            }
                        }

                        // Update the document with the modified projects array
                        userDocRef.update("projects", projects).get();
                    }

                    // Return the ID of the created custom view
                    return customViewDocRef.getId();
                } else {
                    System.out.println("User document does not exist");
                    return "";
                }
            } catch (Exception e) {
                System.out.println("Error creating custom view: " + e.getMessage());
                return "";
            }
        });
    }

    @Async
    public CompletableFuture<Map<String,Object>> getCustomViewData(String customViewId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentSnapshot snapshot = customViewsCollection.document(customViewId).get().get();
                if (snapshot.exists()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("data", snapshot.getData());
                    return result;
                }
                return Collections.emptyMap();
            } catch (Exception e) {
                throw new RuntimeException("Error retrieving custom view: " + e.getMessage(), e);
            }
        });
    }

}