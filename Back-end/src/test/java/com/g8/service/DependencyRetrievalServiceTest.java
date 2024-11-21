package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.model.ExternalDependencyInfo;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.common.reflect.TypeToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DependencyRetrievalServiceTest {

    private Firestore mockFirestore;
    private CollectionReference mockCollectionReference;
    private DocumentReference mockDocumentReference;
    private ApiFuture<DocumentSnapshot> mockFuture;
    private DocumentSnapshot mockDocumentSnapshot;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {

        mockFirestore = mock(Firestore.class);
        mockCollectionReference = mock(CollectionReference.class);
        mockDocumentReference = mock(DocumentReference.class);
        mockFuture = mock(ApiFuture.class);
        mockDocumentSnapshot = mock(DocumentSnapshot.class);
    }

    @Test
    void testGetInternalDependencies() throws Exception {
        List<Map<String, Object>> intDepData = List.of(
                Map.of("name", "ClassA"),
                Map.of("name", "ClassB")
        );

        when(mockFirestore.collection("projects")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.get("intDep")).thenReturn(intDepData);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {

            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            String result = DependencyRetrievalService.getInternalDependencies("testProject");

            Type type = new TypeToken<List<ClassInfo>>() {}.getType();
            List<ClassInfo> expectedList = gson.fromJson(gson.toJson(intDepData), type);

            assertEquals(gson.toJson(expectedList), result);
        }
    }

    @Test
    void testGetClassList() throws Exception {
        List<Map<String, Object>> intDepData = List.of(
                Map.of("name", "ClassA"),
                Map.of("name", "ClassB")
        );

        when(mockFirestore.collection("projects")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.get("classList")).thenReturn(intDepData);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            String result = DependencyRetrievalService.getClassList("testProject");

            assertEquals("ClassA,ClassB", result);
        }
    }

    @Test
    void testGetExternalDependencies() throws Exception {
        List<Map<String, Object>> extDepData = List.of(
                Map.of("artifactId", "ExternalLibA"),
                Map.of("artifactId", "ExternalLibB")
        );

        when(mockFirestore.collection("projects")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.get("extDep")).thenReturn(extDepData);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            String result = DependencyRetrievalService.getExternalDependencies("testProject");

            Type type = new TypeToken<List<ExternalDependencyInfo>>() {}.getType();
            List<ExternalDependencyInfo> expectedList = gson.fromJson(gson.toJson(extDepData), type);

            assertEquals(gson.toJson(expectedList), result);
        }
    }

    @Test
    void testSaveData() throws ExecutionException, InterruptedException {
        List<Map<String, Object>> internalDependencies = List.of(
                Map.of("name", "ClassA"),
                Map.of("name", "ClassB")
        );

        List<Map<String, String>> externalDependencies = List.of(
                Map.of("artifactId", "ExternalLibA"),
                Map.of("artifactId", "ExternalLibB")
        );

        when(mockFirestore.collection("projects")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockCollectionReference.document()).thenReturn(mockDocumentReference);
        when(mockDocumentReference.getId()).thenReturn("mock-id");

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            DependencyRetrievalService.saveData(internalDependencies, externalDependencies);

            verify(mockDocumentReference, times(1)).set(any());
        }
    }
}
