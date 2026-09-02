package com.iagen.agent.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouterServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private RouterService routerService;

    @BeforeEach
    void setUp() {
        routerService = new RouterService(chatClient);
    }

    @Test
    void route_returnsDecision() {
        String json = "{\n" +
                "  \"route\": \"RAG\",\n" +
                "  \"reasoning\": \"Ceci est un test\"\n" +
                "}";

        when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenReturn(json);

        RoutingDecision decision = routerService.route("Test");

        assertThat(decision.route()).isEqualTo(RoutingDecision.Route.RAG);
        assertThat(decision.reasoning()).isEqualTo("Ceci est un test");
    }

    @Test
    void route_invalidJson() {
        String json = "Je ne suis pas du JSON";

        when(chatClient.prompt().system(anyString()).user(anyString()).call().content()).thenReturn(json);

        RoutingDecision decision = routerService.route("Test");

        assertThat(decision.route()).isEqualTo(RoutingDecision.Route.OUT_OF_SCOPE);
        assertThat(decision.reasoning()).isEqualTo("Aucun raisonnement fourni.");
    }
}
