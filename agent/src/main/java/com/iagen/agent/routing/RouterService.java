package com.iagen.agent.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service de routage : analyse l'intention de la question via un LLM à température zéro
 * et retourne une décision de routage JSON traçable.
 * <p>
 * Utilise le ChatClient "router" (temperature=0, sans outils) pour des décisions déterministes.
 * En cas d'erreur de parsing JSON, retourne un fallback {@code OUT_OF_SCOPE} pour éviter de planter.
 */
@Service
public class RouterService {

    private static final Logger log = LoggerFactory.getLogger(RouterService.class);

    private static final String SYSTEM_PROMPT = """
            Tu es un routeur d'agent IA. Ton unique rôle est d'analyser la question de l'utilisateur
            et de décider quelle source utiliser pour y répondre. Tu dois répondre UNIQUEMENT en JSON valide,
            sans aucun texte avant ou après le JSON.
            
            Sources disponibles :
            - RAG : corpus documentaire interne d'iAgen (politique RH, catalogue produits, sécurité informatique)
            - MCP : outils externes (météo d'une ville, calcul de prêt immobilier, annuaire employés)
            - HYBRID : la question nécessite à la fois le corpus documentaire ET un outil externe
            - OUT_OF_SCOPE : question hors-sujet, sans rapport avec les sources disponibles
            
            Règles de routage :
            1. Si la question concerne la politique de l'entreprise, les RH, les produits, la sécurité → RAG
            2. Si la question concerne la météo d'une ville → MCP (WeatherTool)
            3. Si la question concerne un calcul de prêt immobilier → MCP (LoanCalculatorTool)
            4. Si la question demande à trouver un employé → MCP (EmployeeDirectoryTool)
            5. Si la question mélange politique interne ET outil externe → HYBRID
            6. Sinon → OUT_OF_SCOPE
            
            Format de réponse OBLIGATOIRE (JSON strict) :
            {"route": "RAG", "reasoning": "La question porte sur la politique de congés, information disponible dans le corpus RH."}
            
            Les valeurs autorisées pour 'route' sont exactement : RAG, MCP, HYBRID, OUT_OF_SCOPE
            """;

    private final ChatClient routerClient;
    private final ObjectMapper objectMapper;

    public RouterService(@Qualifier("routerChatClient") ChatClient routerClient) {
        this.routerClient = routerClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyse la question et retourne la décision de routage.
     *
     * @param question la question de l'utilisateur
     * @return la décision de routage avec raisonnement traçable
     */
    public RoutingDecision route(String question) {
        log.info("[ROUTER] Analyse de la question : '{}'", question);

        try {
            String rawResponse = routerClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(question)
                    .call()
                    .content();

            log.debug("[ROUTER] Réponse brute LLM : {}", rawResponse);

            // Extraction du JSON (au cas où le LLM ajoute du texte autour)
            String json = extractJson(rawResponse);
            JsonNode node = objectMapper.readTree(json);

            String routeStr = node.path("route").asText("OUT_OF_SCOPE").toUpperCase().trim();
            String reasoning = node.path("reasoning").asText("Aucun raisonnement fourni.");

            RoutingDecision.Route route = parseRoute(routeStr);
            log.info("[ROUTER] Décision : {} | Raisonnement : {}", route, reasoning);
            return new RoutingDecision(route, reasoning);

        } catch (Exception e) {
            log.error("[ROUTER] Erreur de parsing JSON — fallback OUT_OF_SCOPE : {}", e.getMessage());
            return new RoutingDecision(
                    RoutingDecision.Route.OUT_OF_SCOPE,
                    "Erreur de routage interne — réponse non analysable."
            );
        }
    }

    /** Extrait le premier objet JSON trouvé dans une chaîne de texte. */
    private String extractJson(String text) {
        if (text == null) return "{}";
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    /** Parse la route avec fallback sur OUT_OF_SCOPE. */
    private RoutingDecision.Route parseRoute(String routeStr) {
        try {
            return RoutingDecision.Route.valueOf(routeStr);
        } catch (IllegalArgumentException e) {
            log.warn("[ROUTER] Route inconnue '{}' — fallback OUT_OF_SCOPE", routeStr);
            return RoutingDecision.Route.OUT_OF_SCOPE;
        }
    }
}
