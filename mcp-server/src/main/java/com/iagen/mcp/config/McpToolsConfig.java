package com.iagen.mcp.config;

import com.iagen.mcp.tools.EmployeeDirectoryTool;
import com.iagen.mcp.tools.LoanCalculatorTool;
import com.iagen.mcp.tools.WeatherTool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des outils MCP exposés par le serveur.
 * Le {@link MethodToolCallbackProvider} scanne les méthodes annotées {@code @Tool}
 * et les enregistre automatiquement comme outils MCP disponibles.
 */
@Configuration
public class McpToolsConfig {

    /**
     * Fournit les callbacks des outils au runtime MCP Spring AI.
     * Les trois domaines exposés :
     * - WeatherTool        : Web/API (open-meteo)
     * - LoanCalculatorTool : Calcul mathématique
     * - EmployeeDirectoryTool : Fichier/CSV
     */
    @Bean
    public MethodToolCallbackProvider mcpTools(
            WeatherTool weatherTool,
            LoanCalculatorTool loanCalculatorTool,
            EmployeeDirectoryTool employeeDirectoryTool) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherTool, loanCalculatorTool, employeeDirectoryTool)
                .build();
    }
}
