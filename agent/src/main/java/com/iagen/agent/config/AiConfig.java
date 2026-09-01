package com.iagen.agent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;

/**
 * Configuration principale des beans Spring AI.
 * <p>
 * Expose :
 * <ul>
 *   <li>Un {@link ChatClient} "router" à température zéro (décision déterministe)</li>
 *   <li>Un {@link ChatClient} "executor" avec les outils MCP injectés</li>
 *   <li>Un {@link SimpleVectorStore} persisté sur disque</li>
 * </ul>
 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Value("${rag.vectorstore-path:data/vectorstore.json}")
    private String vectorStorePath;

    /**
     * ChatClient "router" : température 0 pour des décisions de routage déterministes.
     * N'a aucun outil attaché — son rôle se limite à analyser l'intention.
     */
    @Bean(name = "routerChatClient")
    public ChatClient routerChatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions.builder().temperature(0.0))
                .build();
    }

    /**
     * ChatClient "executor" : température modérée pour des réponses naturelles.
     * Dispose des outils MCP pour pouvoir les appeler de façon autonome.
     */
    @Bean(name = "executorChatClient")
    @Primary
    public ChatClient executorChatClient(ChatClient.Builder builder,
                                         SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        return builder
                .defaultOptions(ChatOptions.builder().temperature(0.7))
                .defaultTools(mcpToolCallbackProvider)
                .build();
    }

    /**
     * VectorStore in-memory avec persistance sur disque JSON.
     * Chargé depuis le fichier existant au démarrage si disponible.
     * Sauvegardé lors de l'ingestion et au shutdown.
     */
    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(vectorStorePath);
        if (file.exists()) {
            log.info("[RAG] Chargement du VectorStore depuis : {}", file.getAbsolutePath());
            store.load(file);
        } else {
            log.info("[RAG] Aucun VectorStore existant — sera créé lors de la première ingestion.");
        }
        return store;
    }
}
