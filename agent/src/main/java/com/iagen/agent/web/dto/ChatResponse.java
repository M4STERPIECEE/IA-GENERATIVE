package com.iagen.agent.web.dto;

import java.util.List;

/**
 * Réponse complète de l'agent IA.
 * Inclut la réponse finale, la route utilisée, le raisonnement de routage,
 * les sources citées et la trace complète d'exécution.
 */
public class ChatResponse {

    /** La réponse finale générée par l'agent. */
    private final String answer;

    /** La route utilisée : RAG, MCP, HYBRID, OUT_OF_SCOPE. */
    private final String route;

    /** Le raisonnement de routage du LLM router. */
    private final String reasoning;

    /** Les fichiers sources cités (pertinents uniquement pour RAG et HYBRID). */
    private final List<String> sources;

    /** La trace horodatée de l'exécution complète. */
    private final List<String> trace;

    private ChatResponse(Builder builder) {
        this.answer = builder.answer;
        this.route = builder.route;
        this.reasoning = builder.reasoning;
        this.sources = builder.sources;
        this.trace = builder.trace;
    }

    public String getAnswer() { return answer; }
    public String getRoute() { return route; }
    public String getReasoning() { return reasoning; }
    public List<String> getSources() { return sources; }
    public List<String> getTrace() { return trace; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String answer;
        private String route;
        private String reasoning;
        private List<String> sources = List.of();
        private List<String> trace = List.of();

        public Builder answer(String answer) { this.answer = answer; return this; }
        public Builder route(String route) { this.route = route; return this; }
        public Builder reasoning(String reasoning) { this.reasoning = reasoning; return this; }
        public Builder sources(List<String> sources) { this.sources = sources != null ? sources : List.of(); return this; }
        public Builder trace(List<String> trace) { this.trace = trace != null ? trace : List.of(); return this; }

        public ChatResponse build() { return new ChatResponse(this); }
    }
}
