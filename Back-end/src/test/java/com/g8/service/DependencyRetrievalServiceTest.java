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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DependencyRetrievalServiceTest {

    @Mock
    private Firestore mockFirestore;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private DocumentReference mockDocumentReference;
    @Mock
    private ApiFuture<DocumentSnapshot> mockFuture;
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @InjectMocks
    private DependencyRetrievalService dependencyRetrievalService;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        mockFirestore = mock(Firestore.class);
        mockCollectionReference = mock(CollectionReference.class);
        mockDocumentReference = mock(DocumentReference.class);
        mockFuture = mock(ApiFuture.class);
        mockDocumentSnapshot = mock(DocumentSnapshot.class);
        when(mockFirestore.collection("projects")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
        dependencyRetrievalService = new DependencyRetrievalService(mockFirestore);
    }

    @Test
    void testGetInternalDependencies() throws Exception {
        List<Map<String, Object>> intDepData = List.of(
                Map.of("name", "ClassA"),
                Map.of("name", "ClassB")
        );


        when(mockDocumentSnapshot.get("intDep")).thenReturn(intDepData);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {

            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getInternalDependencies("testProject");
            result.join();


            Type type = new TypeToken<List<ClassInfo>>() {}.getType();
            List<ClassInfo> expectedList = gson.fromJson(gson.toJson(intDepData), type);

            assertEquals(gson.toJson(expectedList), result.get());
        }
    }

    @Test
    void testGetClassList() throws Exception {
        List<Map<String, Object>> intDepData = List.of(
                Map.of("name", "ClassA"),
                Map.of("name", "ClassB")
        );

        when(mockDocumentSnapshot.get("intDep")).thenReturn(intDepData);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getClassList("testProject");
            result.join();

            assertEquals("ClassA,ClassB", result.get());
        }
    }

    @Test
    void testGetExternalDependencies() throws Exception {
        List<Map<String, Object>> extDepData = List.of(
                Map.of("artifactId", "ExternalLibA"),
                Map.of("artifactId", "ExternalLibB")
        );

        when(mockDocumentSnapshot.get("extDep")).thenReturn(extDepData);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getExternalDependencies("testProject");
            result.join();

            Type type = new TypeToken<List<ExternalDependencyInfo>>() {}.getType();
            List<ExternalDependencyInfo> expectedList = gson.fromJson(gson.toJson(extDepData), type);

            assertEquals(gson.toJson(expectedList), result.get());
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

        when(mockCollectionReference.document()).thenReturn(mockDocumentReference);
        when(mockDocumentReference.getId()).thenReturn("mock-id");

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> response =  dependencyRetrievalService.saveData(internalDependencies, externalDependencies);
            response.join();

            verify(mockDocumentReference, times(1)).set(any());
        }
    }
}
