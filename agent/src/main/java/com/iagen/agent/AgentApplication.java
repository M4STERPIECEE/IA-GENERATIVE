package com.iagen.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Agent IA — Orchestrateur principal.
 * Pipeline : RouterService → RagService / McpClient → OrchestratorService → réponse.
 * Port : 8080
 */
@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
