package com.iagen.agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final SimpleVectorStore vectorStore;

    @Value("${rag.min-similarity:0.5}")
    private double minSimilarity;

    @Value("${rag.top-k:4}")
    private int topK;

    public RagService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public record RagResult(String contextBlock, List<String> sources, boolean notInCorpus) {}

    public RagResult retrieve(String question) {
        log.info("[RAG] Retrieval pour : '{}'", question);

        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(minSimilarity)
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);

        if (docs == null || docs.isEmpty()) {
            log.warn("[RAG] Aucun document pertinent trouvé (seuil={}, topK={})", minSimilarity, topK);
            return new RagResult("", List.of(), true);
        }

        log.info("[RAG] {} document(s) récupéré(s) au-dessus du seuil {}", docs.size(), minSimilarity);

        StringBuilder ctx = new StringBuilder();
        List<String> sources = docs.stream()
                .map(d -> (String) d.getMetadata().getOrDefault("source", "inconnu"))
                .distinct()
                .toList();

        for (Document doc : docs) {
            Map<String, Object> meta = doc.getMetadata();
            String source = (String) meta.getOrDefault("source", "inconnu");
            ctx.append("[source: ").append(source).append("]\n");
            ctx.append(doc.getText()).append("\n\n");
        }

        return new RagResult(ctx.toString().trim(), sources, false);
    }
}
