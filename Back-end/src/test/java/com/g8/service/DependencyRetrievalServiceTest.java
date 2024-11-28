package com.g8.service;

import com.g8.model.ClassInfo;
import com.g8.model.ExternalDependencyInfo;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.reflect.TypeToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DependencyRetrievalServiceTest {

    @Mock
    private Firestore mockFirestore;

    @Mock
    private CollectionReference projectCollectionReference, userProjectCollectionReference, customViewsCollectionReference;

    @Mock
    private DocumentReference mockDocumentReference;


    @Mock
    private ApiFuture<DocumentSnapshot> mockFuture;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private ApiFuture<WriteResult> mockWriteResult;

    @InjectMocks
    private DependencyRetrievalService dependencyRetrievalService;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {

        MockitoAnnotations.openMocks(this);
        mockFirestore = mock(Firestore.class);
        projectCollectionReference = mock(CollectionReference.class);
        userProjectCollectionReference = mock(CollectionReference.class);
        customViewsCollectionReference = mock(CollectionReference.class);
        mockDocumentReference = mock(DocumentReference.class); 
        mockFuture = mock(ApiFuture.class);
        mockDocumentSnapshot = mock(DocumentSnapshot.class);

        when(mockFirestore.collection("projects")).thenReturn(projectCollectionReference);
        when(mockFirestore.collection("user_projects")).thenReturn(userProjectCollectionReference);
        when(mockFirestore.collection("custom_views")).thenReturn(customViewsCollectionReference);

        when(projectCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentReference.set(any())).thenReturn(mockWriteResult);

        when(projectCollectionReference.document("testProject")).thenReturn(mockDocumentReference);
        when(userProjectCollectionReference.document("testUser")).thenReturn(mockDocumentReference);

        when(mockDocumentReference.update(anyString(), any(), any())).thenReturn(mockWriteResult);
        when(mockDocumentReference.set(any(), any(SetOptions.class))).thenReturn(mockWriteResult);

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
        List<String> classList = List.of("ClassA", "ClassB");

        when(mockDocumentSnapshot.get("classList")).thenReturn(classList);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getClassList("testProject");
            result.join();

            assertEquals(gson.toJson(classList), result.get());
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

        List<String> classList = List.of("ClassA", "ClassB");

        when(projectCollectionReference.document()).thenReturn(mockDocumentReference);
        when(mockDocumentReference.getId()).thenReturn("mock-id");

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> response =  dependencyRetrievalService.saveData(internalDependencies, externalDependencies, classList);
            response.join();

            verify(mockDocumentReference, times(1)).set(any());
        }
    }

    @Test
    void testGetInternalDependenciesWithNullData() throws Exception {
        when(mockDocumentSnapshot.get("intDep")).thenReturn(null);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getInternalDependencies("testProject");
            result.join();

            assertEquals(null, result.get());
        }
    }

    @Test
    void testGetInternalDependenciesWithEmptyData() throws Exception {
        when(mockDocumentSnapshot.get("intDep")).thenReturn(List.of());

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getInternalDependencies("testProject");
            result.join();

            assertEquals(null, result.get());
        }
    }

    @Test
    void testGetClassListWithNullData() throws Exception {
        when(mockDocumentSnapshot.get("classList")).thenReturn(null);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getClassList("testProject");
            result.join();

            assertEquals(null, result.get());
        }
    }

    @Test
    void testGetClassListWithEmptyData() throws Exception {
        when(mockDocumentSnapshot.get("classList")).thenReturn(List.of());

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getClassList("testProject");
            result.join();

            assertEquals(null, result.get());
        }
    }

    @Test
    void testGetExternalDependenciesWithNullData() throws Exception {
        when(mockDocumentSnapshot.get("extDep")).thenReturn(null);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getExternalDependencies("testProject");
            result.join();

            assertEquals(null, result.get());
        }
    }

    @Test
    void testGetExternalDependenciesWithEmptyData() throws Exception {
        when(mockDocumentSnapshot.get("extDep")).thenReturn(List.of());

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            CompletableFuture<String> result = dependencyRetrievalService.getExternalDependencies("testProject");
            result.join();

            assertEquals(null, result.get());
        }
    }

    @Test
    void testGetInternalDependenciesThrowsException() {
        when(mockDocumentReference.get()).thenThrow(new RuntimeException("Firestore error"));

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            assertThrows(RuntimeException.class, () -> {
                dependencyRetrievalService.getInternalDependencies("testProject").join();
            });
        }
    }

    @Test
    void testSaveDataThrowsException() throws ExecutionException, InterruptedException {
        when(mockDocumentReference.set(any())).thenThrow(new RuntimeException("Firestore write error"));

        List<Map<String, Object>> internalDependencies = List.of();
        List<Map<String, String>> externalDependencies = List.of();
        List<String> classList = List.of();

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(mockFirestore);

            assertThrows(RuntimeException.class, () -> {
                dependencyRetrievalService.saveData(internalDependencies, externalDependencies, classList).join();
            });
        }
    }

    @Test
    void testSaveProjectToUser() throws Exception {
        String testProjectId = "testProjectId";
        String testUserId = "testUserId";
    
        // Prepare project info map for verification
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("projectId", testProjectId);
        projectInfo.put("custom_view", "");
    
        // Case 1: User document exists
        {
            // Reset mocks
            reset(projectCollectionReference, mockDocumentReference, mockFuture, mockDocumentSnapshot);
    
            // Setup mocks for existing document scenario
            when(mockFirestore.collection("user_projects")).thenReturn(userProjectCollectionReference);
            when(userProjectCollectionReference.document(testUserId)).thenReturn(mockDocumentReference);
            
            // Simulate existing document
            when(mockDocumentReference.get()).thenReturn(mockFuture);
            when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
            when(mockDocumentSnapshot.exists()).thenReturn(true);
    
            // Mock the update method
            when(mockDocumentReference.update(eq("projects"), any(FieldValue.class)))
                .thenReturn(mock(ApiFuture.class));
    
            // Execute and verify
            CompletableFuture<Void> result = dependencyRetrievalService.saveProjectToUser(testProjectId, testUserId);
            result.join();
    
            verify(mockDocumentReference, times(1)).update(eq("projects"), any(FieldValue.class));
        }
    
        // Case 2: User document does not exist
        {
            // Reset mocks
            reset(projectCollectionReference, mockDocumentReference, mockFuture, mockDocumentSnapshot);
    
            // Setup mocks for non-existing document scenario
            when(mockFirestore.collection("user_projects")).thenReturn(userProjectCollectionReference);
            when(userProjectCollectionReference.document(testUserId)).thenReturn(mockDocumentReference);
            
            // Simulate non-existing document
            when(mockDocumentReference.get()).thenReturn(mockFuture);
            when(mockFuture.get()).thenReturn(mockDocumentSnapshot);
            when(mockDocumentSnapshot.exists()).thenReturn(false);
    
            // Mock the set method
            when(mockDocumentReference.set(any(Map.class), eq(SetOptions.merge())))
                .thenReturn(mock(ApiFuture.class));
    
            // Execute and verify
            CompletableFuture<Void> result = dependencyRetrievalService.saveProjectToUser(testProjectId, testUserId);
            result.join();
    
            verify(mockDocumentReference, times(1)).set(
                argThat(argument -> {
                    if (argument instanceof Map) {
                        Map<String, Object> userData = (Map<String, Object>) argument;
                        List<Map<String, Object>> projects = (List<Map<String, Object>>) userData.get("projects");
                        return projects != null && 
                               projects.size() == 1 && 
                               projects.get(0).get("projectId").equals(testProjectId);
                    }
                    return false;
                }), 
                eq(SetOptions.merge())
            );
        }
    }

    @Test
    void testGetUserProjects_withExistingUserIdAndProjects() throws Exception {
        // Arrange
        String userId = "testUser";
        List<Map<String, Object>> mockProjects = List.of(
            Map.of("projectId", "123", "projectName", "Project A"),
            Map.of("projectId", "456", "projectName", "Project B")
        );
        DocumentSnapshot mockSnapshot = Mockito.mock(DocumentSnapshot.class);
        Mockito.when(mockSnapshot.exists()).thenReturn(true);
        Mockito.when(mockSnapshot.get("projects")).thenReturn(mockProjects);
    
        ApiFuture<DocumentSnapshot> mockApiFuture = Mockito.mock(ApiFuture.class);
        Mockito.when(mockApiFuture.get()).thenReturn(mockSnapshot);
        Mockito.when(userProjectCollectionReference.document(userId).get()).thenReturn(mockApiFuture);
    
        // Act
        CompletableFuture<List<Map<String, Object>>> result = dependencyRetrievalService.getUserProjects(userId);
    
        // Assert
        Assertions.assertEquals(mockProjects, result.get());
    }

    @Test
    void testGetUserProjects_withExistingUserIdAndNoProjects() throws Exception {
        // Arrange
        String userId = "testUser";
        DocumentSnapshot mockSnapshot = Mockito.mock(DocumentSnapshot.class);
        Mockito.when(mockSnapshot.exists()).thenReturn(true);
        Mockito.when(mockSnapshot.get("projects")).thenReturn(null);

        ApiFuture<DocumentSnapshot> mockApiFuture = Mockito.mock(ApiFuture.class);
        Mockito.when(mockApiFuture.get()).thenReturn(mockSnapshot);
        Mockito.when(userProjectCollectionReference.document(userId).get()).thenReturn(mockApiFuture);

        // Act
        CompletableFuture<List<Map<String, Object>>> result = dependencyRetrievalService.getUserProjects(userId);

        // Assert
        Assertions.assertEquals(Collections.emptyList(), result.get());
    }
    
    @Test
    void testCreateCustomView_UserDocumentNotFound() throws Exception {
        String userId = "testUser";
        String projectId = "testProject";
        Map<String, Object> customViewData = new HashMap<>();
        customViewData.put("data", customViewData);
        DocumentReference customViewRefDoc = mock(DocumentReference.class);
        when(customViewsCollectionReference.document()).thenReturn(customViewRefDoc);


        // Mocking Firestore operations for user document not found
        DocumentReference userDocRef = mock(DocumentReference.class);
        when(userProjectCollectionReference.document(userId)).thenReturn(userDocRef);
        ApiFuture<DocumentSnapshot> userDocFuture = mock(ApiFuture.class);
        when(userDocRef.get()).thenReturn(userDocFuture);
        DocumentSnapshot userDocSnapshot = mock(DocumentSnapshot.class);
        when(userDocFuture.get()).thenReturn(userDocSnapshot);
        when(userDocSnapshot.exists()).thenReturn(false);

        // Call the method
        String customViewId = dependencyRetrievalService.createCustomView(userId, projectId, customViewData).get();

        // Assertions
        verify(customViewRefDoc).set(customViewData);
        assertTrue(customViewId.isEmpty());
    }

    @Test
    void testCreateCustomView_Success() throws Exception {
        String userId = "testUser";
        String projectId = "testProject";
        Map<String, Object> customViewData = new HashMap<>();
        customViewData.put("key", "value");

        // Mocking Firestore operations
        DocumentReference customViewDocRef = mock(DocumentReference.class);
        when(customViewsCollectionReference.document()).thenReturn(customViewDocRef);
        ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
        when(customViewDocRef.set(any())).thenReturn(writeResultFuture);
        when(customViewDocRef.getId()).thenReturn("customView123");
        when(writeResultFuture.get()).thenReturn(mock(WriteResult.class));

        // Mocking user document existence and updates
        DocumentReference userDocRef = mock(DocumentReference.class);
        when(userProjectCollectionReference.document(userId)).thenReturn(userDocRef);
        ApiFuture<DocumentSnapshot> userDocFuture = mock(ApiFuture.class);
        when(userDocRef.get()).thenReturn(userDocFuture);
        DocumentSnapshot userDocSnapshot = mock(DocumentSnapshot.class);
        when(userDocFuture.get()).thenReturn(userDocSnapshot);
        when(userDocSnapshot.exists()).thenReturn(true);
        when(userDocSnapshot.get("projects")).thenReturn(Collections.emptyList());

        ApiFuture<WriteResult> updateWriteResult = mock(ApiFuture.class);
        when(userDocRef.update(anyString(), any(), any())).thenReturn(updateWriteResult);
        when(updateWriteResult.get()).thenReturn(mock(WriteResult.class));

        // Call the method
        String customViewId = dependencyRetrievalService.createCustomView(userId, projectId, customViewData).get();

        // Assertions
        assertNotNull(customViewId);
        verify(customViewsCollectionReference).document();
        verify(customViewDocRef).set(any());
        verify(userDocRef).update("projects", Collections.emptyList());
    }

    @Test
    void testGetCustomViewData_Success() throws Exception {
        String customViewId = "customView123";

        // Mocking Firestore operations
        DocumentReference customViewDocRef = mock(DocumentReference.class);
        when(customViewsCollectionReference.document(customViewId)).thenReturn(customViewDocRef);
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = mock(ApiFuture.class);
        when(customViewDocRef.get()).thenReturn(documentSnapshotApiFuture);
        DocumentSnapshot customViewDocSnapshot = mock(DocumentSnapshot.class);
        when(documentSnapshotApiFuture.get()).thenReturn(customViewDocSnapshot);
        when(customViewDocSnapshot.exists()).thenReturn(true);
        when(customViewDocSnapshot.getData()).thenReturn(Collections.singletonMap("dataKey", "dataValue"));

        // Call the method
        Map<String, Object> customViewData = dependencyRetrievalService.getCustomViewData(customViewId).get();

        // Assertions
        assertNotNull(customViewData);
        assertTrue(customViewData.containsKey("data"));
        assertEquals("dataValue", ((Map) customViewData.get("data")).get("dataKey"));
        verify(customViewsCollectionReference).document(customViewId);
        verify(customViewDocRef).get();
    }

    @Test
    void testGetCustomViewData_NotFound() throws Exception {
        String customViewId = "nonExistentCustomView";

        // Mocking Firestore operations
        DocumentReference customViewDocRef = mock(DocumentReference.class);
        when(customViewsCollectionReference.document(customViewId)).thenReturn(customViewDocRef);
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = mock(ApiFuture.class);
        when(customViewDocRef.get()).thenReturn(documentSnapshotApiFuture);
        DocumentSnapshot customViewDocSnapshot = mock(DocumentSnapshot.class);
        when(documentSnapshotApiFuture.get()).thenReturn(customViewDocSnapshot);
        when(customViewDocSnapshot.exists()).thenReturn(false);

        // Call the method
        Map<String, Object> customViewData = dependencyRetrievalService.getCustomViewData(customViewId).get();

        // Assertions
        assertTrue(customViewData.isEmpty());
        verify(customViewsCollectionReference).document(customViewId);
        verify(customViewDocRef).get();
    }




}