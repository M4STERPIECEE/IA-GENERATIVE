package com.iagen.agent.routing;

/**
 * Décision de routage retournée par le {@link RouterService}.
 *
 * @param route     la route choisie : RAG, MCP, HYBRID ou OUT_OF_SCOPE
 * @param reasoning le raisonnement traçable du LLM router
 */
public record RoutingDecision(Route route, String reasoning) {

    /** Routes possibles pour l'orchestrateur. */
    public enum Route {
        /** Répondre uniquement à partir du corpus documentaire interne (RAG). */
        RAG,
        /** Appeler un ou plusieurs outils MCP externes. */
        MCP,
        /** Combiner RAG et MCP dans la même réponse. */
        HYBRID,
        /** Question hors-sujet ou sans réponse possible dans le système. */
        OUT_OF_SCOPE
    }
}
