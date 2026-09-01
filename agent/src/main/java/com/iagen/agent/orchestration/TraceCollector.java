package com.iagen.agent.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Collecteur de trace d'exécution de l'agent.
 * Enregistre chaque étape horodatée (routage, retrieval, appels outils, erreurs)
 * pour la retourner dans la réponse API et offrir une traçabilité complète.
 * <p>
 * Une nouvelle instance doit être créée par requête (scope request ou injection manuelle).
 */
public class TraceCollector {

    private static final Logger log = LoggerFactory.getLogger(TraceCollector.class);
    private final List<TraceEntry> entries = new ArrayList<>();

    /**
     * Ajoute une étape de trace.
     *
     * @param prefix  préfixe de log (ex: ROUTER, RAG, MCP, ORCHESTRATOR)
     * @param message description de l'étape
     */
    public void add(String prefix, String message) {
        String entry = "[" + prefix + "] " + message;
        entries.add(new TraceEntry(Instant.now().toString(), entry));
        log.debug("{}", entry);
    }

    /** Retourne la liste complète des entrées de trace pour la réponse API. */
    public List<String> getEntries() {
        return entries.stream()
                .map(e -> e.timestamp() + " | " + e.message())
                .toList();
    }

    /** Entrée de trace horodatée. */
    private record TraceEntry(String timestamp, String message) {}
}
