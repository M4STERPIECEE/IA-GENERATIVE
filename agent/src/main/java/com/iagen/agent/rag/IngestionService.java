package com.iagen.agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service d'ingestion du corpus documentaire dans le VectorStore.
 * Lancé au démarrage via {@link ApplicationRunner}.
 * <p>
 * Comportement idempotent : si le VectorStore est déjà peuplé
 * (chargé depuis le fichier JSON persisté), l'ingestion est sautée.
 */
@Service
public class IngestionService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private static final int CHUNK_SIZE = 512;
    private static final int CHUNK_OVERLAP = 64;

    private final SimpleVectorStore vectorStore;

    @Value("${rag.vectorstore-path:data/vectorstore.json}")
    private String vectorStorePath;

    public IngestionService(SimpleVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File persistedFile = new File(vectorStorePath);

        if (persistedFile.exists() && persistedFile.length() > 100) {
            log.info("[RAG][Ingestion] VectorStore déjà persisté ({} octets) — ingestion sautée.", persistedFile.length());
            return;
        }

        log.info("[RAG][Ingestion] Démarrage de l'ingestion du corpus documentaire...");
        List<Document> allChunks = new ArrayList<>();

        // Scan du dossier docs/ à la racine du projet
        Path docsPath = Paths.get("docs");
        if (!Files.exists(docsPath)) {
            log.warn("[RAG][Ingestion] Dossier 'docs/' introuvable. Aucun document ingéré.");
            return;
        }

        // TokenTextSplitter avec les paramètres de chunking
        TokenTextSplitter splitter = new TokenTextSplitter(CHUNK_SIZE, CHUNK_OVERLAP, 5, 10000, true, List.of());

        try (Stream<Path> paths = Files.walk(docsPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".md") || p.toString().endsWith(".txt"))
                    .forEach(filePath -> {
                        try {
                            log.info("[RAG][Ingestion] Traitement : {}", filePath);
                            List<Document> docs = readMarkdown(filePath);
                            List<Document> chunks = splitter.apply(docs);

                            // Ajout des métadonnées de citation
                            for (int i = 0; i < chunks.size(); i++) {
                                chunks.get(i).getMetadata().put("source", filePath.getFileName().toString());
                                chunks.get(i).getMetadata().put("chunk_index", String.valueOf(i));
                            }

                            allChunks.addAll(chunks);
                            log.info("[RAG][Ingestion] {} chunks créés depuis '{}'", chunks.size(), filePath.getFileName());
                        } catch (Exception e) {
                            log.error("[RAG][Ingestion] Erreur sur {} : {}", filePath, e.getMessage());
                        }
                    });
        }

        if (allChunks.isEmpty()) {
            log.warn("[RAG][Ingestion] Aucun chunk créé — vérifiez le dossier docs/");
            return;
        }

        log.info("[RAG][Ingestion] Indexation de {} chunks dans le VectorStore...", allChunks.size());
        vectorStore.add(allChunks);

        // Persistance sur disque
        File parentDir = persistedFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        vectorStore.save(persistedFile);
        log.info("[RAG][Ingestion] VectorStore persisté → {}", persistedFile.getAbsolutePath());
    }

    private List<Document> readMarkdown(Path filePath) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(false)
                .withIncludeCodeBlock(true)
                .withIncludeBlockquote(true)
                .build();
        return new MarkdownDocumentReader(new FileSystemResource(filePath.toFile()), config).get();
    }
}
