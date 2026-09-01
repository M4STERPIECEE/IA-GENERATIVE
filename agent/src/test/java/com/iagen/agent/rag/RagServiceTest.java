package com.iagen.agent.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private SimpleVectorStore vectorStore;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(vectorStore);
    }

    @Test
    void retrieve_documentsFound() {
        Document doc1 = new Document("Content 1", Map.of("source", "doc1.txt"));
        Document doc2 = new Document("Content 2", Map.of("source", "doc2.txt"));
        
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1, doc2));
        
        RagResult result = ragService.retrieve("query");
        
        assertThat(result.notInCorpus()).isFalse();
        assertThat(result.contextBlock()).contains("Content 1").contains("Content 2");
        assertThat(result.sources()).containsExactly("doc1.txt", "doc2.txt");
    }

    @Test
    void retrieve_noDocumentsFound() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        
        RagResult result = ragService.retrieve("query");
        
        assertThat(result.notInCorpus()).isTrue();
        assertThat(result.contextBlock()).isEmpty();
        assertThat(result.sources()).isEmpty();
    }

    @Test
    void retrieve_nullReturned() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(null);
        
        RagResult result = ragService.retrieve("query");
        
        assertThat(result.notInCorpus()).isTrue();
    }

    @Test
    void retrieve_duplicateSources() {
        Document doc1 = new Document("C1", Map.of("source", "doc1.txt"));
        Document doc2 = new Document("C2", Map.of("source", "doc1.txt"));
        
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1, doc2));
        
        RagResult result = ragService.retrieve("query");
        
        assertThat(result.sources()).containsExactly("doc1.txt");
    }

    @Test
    void retrieve_missingSourceMetadata() {
        Document doc1 = new Document("C1", Map.of());
        
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1));
        
        RagResult result = ragService.retrieve("query");
        
        assertThat(result.sources()).containsExactly("inconnu");
    }

    @Test
    void retrieve_singleDocument() {
        Document doc1 = new Document("C1", Map.of("source", "doc.txt"));
        
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1));
        
        RagResult result = ragService.retrieve("query");
        
        assertThat(result.sources()).hasSize(1);
    }
}
