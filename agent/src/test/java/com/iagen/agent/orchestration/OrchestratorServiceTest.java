package com.iagen.agent.orchestration;

import com.iagen.agent.rag.RagResult;
import com.iagen.agent.rag.RagService;
import com.iagen.agent.routing.RoutingDecision;
import com.iagen.agent.security.PromptInjectionGuard;
import com.iagen.agent.web.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceTest {

    @Mock
    private RagService ragService;

    @Mock
    private PromptInjectionGuard promptGuard;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private OrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        orchestratorService = new OrchestratorService(chatClient, ragService, promptGuard);
    }

    @Test
    void orchestrate_outOfScope() {
        RoutingDecision decision = new RoutingDecision(RoutingDecision.Route.OUT_OF_SCOPE, "Hors sujet");
        TraceCollector trace = new TraceCollector();
        
        ChatResponse result = orchestratorService.orchestrate("Question", decision, trace);
        
        assertThat(result.getAnswer()).contains("Je suis l'assistant IA");
    }

    @Test
    void orchestrate_ragSearch() {
        RoutingDecision decision = new RoutingDecision(RoutingDecision.Route.RAG, "RAG");
        RagResult ragResult = new RagResult("Contexte RAG", List.of("source1.txt"), false);
        TraceCollector trace = new TraceCollector();
        
        when(ragService.retrieve("Question")).thenReturn(ragResult);
        when(promptGuard.sanitizeAndWrap(anyString(), eq("RAG"), eq(trace))).thenReturn("Contexte RAG");
        
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenReturn("Réponse RAG");

        ChatResponse result = orchestratorService.orchestrate("Question", decision, trace);
        
        assertThat(result.getAnswer()).isEqualTo("Réponse RAG");
    }

    @Test
    void orchestrate_ragSearchNotInCorpus() {
        RoutingDecision decision = new RoutingDecision(RoutingDecision.Route.RAG, "RAG");
        RagResult ragResult = new RagResult("", List.of(), true);
        TraceCollector trace = new TraceCollector();
        
        when(ragService.retrieve("Question")).thenReturn(ragResult);

        ChatResponse result = orchestratorService.orchestrate("Question", decision, trace);
        
        assertThat(result.getAnswer()).contains("Je ne dispose pas");
    }
}
