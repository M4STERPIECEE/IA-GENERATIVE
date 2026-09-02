package com.iagen.agent.web;

import com.iagen.agent.orchestration.OrchestratorService;
import com.iagen.agent.routing.RouterService;
import com.iagen.agent.routing.RoutingDecision;
import com.iagen.agent.web.dto.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RouterService routerService;

    @Mock
    private OrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(routerService, orchestratorService))
                .addPlaceholderValue("version.path", "v1")
                .setControllerAdvice(new AgentExceptionHandler())
                .build();
    }

    @Test
    void chat_validQuestion() throws Exception {
        RoutingDecision decision = new RoutingDecision(RoutingDecision.Route.RAG, "Test routing");
        when(routerService.route("Bonjour ?")).thenReturn(decision);
        when(orchestratorService.orchestrate(eq("Bonjour ?"), eq(decision), any()))
                .thenReturn(ChatResponse.builder().answer("Ceci est une réponse.").build());

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"Bonjour ?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Ceci est une réponse."));
    }

    @Test
    void chat_emptyQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_nullQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_blankQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_callsRouter() throws Exception {
        RoutingDecision decision = new RoutingDecision(RoutingDecision.Route.OUT_OF_SCOPE, "Test");
        when(routerService.route("Test")).thenReturn(decision);
        when(orchestratorService.orchestrate(eq("Test"), eq(decision), any()))
                .thenReturn(ChatResponse.builder().build());

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"Test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void chat_callsOrchestrator() throws Exception {
        RoutingDecision decision = new RoutingDecision(RoutingDecision.Route.OUT_OF_SCOPE, "Test");
        when(routerService.route("Test")).thenReturn(decision);
        when(orchestratorService.orchestrate(eq("Test"), eq(decision), any()))
                .thenReturn(ChatResponse.builder().answer("Response").build());

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"Test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Response"));
    }

    @Test
    void health_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string("Agent IA opérationnel"));
    }
}
