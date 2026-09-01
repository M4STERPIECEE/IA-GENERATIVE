package com.iagen.agent.web;

import com.iagen.agent.orchestration.OrchestratorService;
import com.iagen.agent.orchestration.TraceCollector;
import com.iagen.agent.routing.RoutingDecision;
import com.iagen.agent.routing.RouterService;
import com.iagen.agent.web.dto.ChatRequest;
import com.iagen.agent.web.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST principal de l'agent IA.
 * Expose l'endpoint {@code POST /api/chat} pour interagir avec l'assistant.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final RouterService routerService;
    private final OrchestratorService orchestratorService;

    public ChatController(RouterService routerService, OrchestratorService orchestratorService) {
        this.routerService = routerService;
        this.orchestratorService = orchestratorService;
    }

    /**
     * Endpoint principal de l'agent IA.
     *
     * @param request la question de l'utilisateur
     * @return la réponse complète avec route, raisonnement, sources et trace
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ChatResponse.builder()
                            .answer("La question ne peut pas être vide.")
                            .route("INVALID_REQUEST")
                            .reasoning("Requête invalide.")
                            .build()
            );
        }

        log.info("[CHAT] Nouvelle question : '{}'", request.question());
        TraceCollector trace = new TraceCollector();

        // Étape 1 : Routage
        RoutingDecision decision = routerService.route(request.question());

        // Étape 2 : Orchestration
        ChatResponse response = orchestratorService.orchestrate(request.question(), decision, trace);

        return ResponseEntity.ok(response);
    }

    /** Endpoint de santé pour vérifier que l'agent est opérationnel. */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Agent iAgen opérationnel ✓");
    }
}
