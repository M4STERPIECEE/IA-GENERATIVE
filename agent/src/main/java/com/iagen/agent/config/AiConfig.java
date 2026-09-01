package com.iagen.agent.config;

import lombok.extern.slf4j.Slf4j;
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

@Configuration
@Slf4j
public class AiConfig {

    @Value("${rag.vectorstore-path:data/vectorstore.json}")
    private String vectorStorePath;

    @Bean(name = "routerChatClient")
    public ChatClient routerChatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions.builder().temperature(0.0))
                .build();
    }

    @Bean(name = "executorChatClient")
    @Primary
    public ChatClient executorChatClient(ChatClient.Builder builder,
            SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        return builder
                .defaultOptions(ChatOptions.builder().temperature(0.7))
                .defaultTools(mcpToolCallbackProvider)
                .build();
    }

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(vectorStorePath);
        if (file.exists()) {
            log.info("Chargement du VectorStore depuis : {}", file.getAbsolutePath());
            store.load(file);
        } else {
            log.info("Aucun VectorStore existant — sera créé lors de la première ingestion.");
        }
        return store;
    }
}
