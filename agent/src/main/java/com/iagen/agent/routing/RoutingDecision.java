package com.iagen.agent.routing;

public record RoutingDecision(Route route, String reasoning) {

    public enum Route {
        RAG,
        MCP,
        HYBRID,
        OUT_OF_SCOPE
    }
}
