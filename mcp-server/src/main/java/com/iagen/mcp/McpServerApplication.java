package com.iagen.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Serveur MCP exposant 3 outils de domaines distincts :
 * - WeatherTool        (domaine WEB/API)
 * - LoanCalculatorTool (domaine CALCUL)
 * - EmployeeDirectoryTool (domaine FICHIER/CSV)
 *
 * Transport : STREAMABLE_HTTP sur le port 8081.
 * Peut être lancé indépendamment de l'agent.
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
