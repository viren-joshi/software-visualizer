package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.model.ExternalDependencyInfo;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.common.reflect.TypeToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyRetrievalService {

    private static final Firestore firestore = FirestoreClient.getFirestore();
    private static final CollectionReference collectionReference = firestore.collection("projects");
    private static final Gson gson = new Gson();

    private DependencyRetrievalService() {}

    public static String getInternalDependencies(String projectId) {

        try {
            Thread.sleep(1000);

            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = collectionReference.document(projectId).get().get();

            List<Map<String, Object>> projects = (List<Map<String, Object>>) projectCollection.get("intDep");

            if (projects != null && !projects.isEmpty()) {

                Type type = new TypeToken<List<ClassInfo>>() {}.getType();

                List<ClassInfo> dependencies = gson.fromJson(gson.toJson(projects), type);

                // Convert the list of ClassInfo objects to JSON
                String jsonOutput = gson.toJson(dependencies);
                return jsonOutput;
            }

            return null;

        }  catch (Exception e) {
            throw new RuntimeException("Error retrieving internal dependencies: " + e.getMessage());
        }
    }

    public static String getClassList(String projectId) {

        try {

            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = collectionReference.document(projectId).get().get();

            List<Map<String, Object>> projects = (List<Map<String, Object>>) projectCollection.get("intDep");

            List<String> classes = new ArrayList<>();

            if (projects != null && !projects.isEmpty()) {

                // Loop through each map and convert to ClassInfo object
                for (Map<String, Object> intDep : projects) {
                    // Deserialize the map into a ClassInfo object
                    ClassInfo info = gson.fromJson(gson.toJson(intDep), ClassInfo.class);
                    classes.add(info.getName());
                }

                return String.join(",", classes);
            }
            return null;

        }  catch (Exception e) {
            throw new RuntimeException("Error retrieving classList: " + e.getMessage());
        }
    }

    public static String getExternalDependencies(String projectId) {

        try {
            // Retrieve the collection with the name `projectId`
            DocumentSnapshot projectCollection = collectionReference.document(projectId).get().get();

            List<Map<String, Object>> projects = (List<Map<String, Object>>) projectCollection.get("extDep");

            if (projects != null && !projects.isEmpty()) {

                Type type = new TypeToken<List<ExternalDependencyInfo>>() {}.getType();

                List<ExternalDependencyInfo> dependencies = gson.fromJson(gson.toJson(projects), type);

                // Convert the list of ClassInfo objects to JSON
                return gson.toJson(dependencies);
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving external dependencies: " + e.getMessage());
        }
    }

    public static String saveData(List<Map<String, Object>> internalDependencies, List<Map<String, String>> externalDependencies) {
        DocumentReference documentReference = collectionReference.document();

        // Prepare data for both internal and external dependencies
        Map<String, Object> dependenciesData = new HashMap<>();
        dependenciesData.put("intDep", internalDependencies);  // Internal dependencies
        dependenciesData.put("extDep", externalDependencies);  // External dependencies

        // Save the data into the Firestore document
        documentReference.set(dependenciesData);

        // Retrieve the generated document ID
        return documentReference.getId();
    }
}