package com.iagen.mcp.tools;

import com.iagen.mcp.security.OutputSanitizer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Outil MCP — Domaine FICHIER/CSV.
 * Recherche dans l'annuaire des employés iAgen (fichier CSV interne).
 * Le CSV est chargé en mémoire au démarrage pour des performances optimales.
 */
@Service
public class EmployeeDirectoryTool {

    private static final Logger log = LoggerFactory.getLogger(EmployeeDirectoryTool.class);
    private static final String CSV_PATH = "data/employees.csv";

    private final OutputSanitizer sanitizer;
    private final List<Employee> employees = new ArrayList<>();

    public EmployeeDirectoryTool(OutputSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    /** Chargement du CSV au démarrage de l'application. */
    @PostConstruct
    void loadCsv() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(CSV_PATH).getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    employees.add(new Employee(
                            parts[0].trim(), parts[1].trim(), parts[2].trim(),
                            parts[3].trim(), parts[4].trim()
                    ));
                }
            }
            log.info("[MCP][EmployeeDirectoryTool] {} employés chargés depuis {}", employees.size(), CSV_PATH);
        } catch (Exception e) {
            log.error("[MCP][EmployeeDirectoryTool] Erreur de chargement du CSV : {}", e.getMessage());
        }
    }

    /**
     * Recherche des employés par nom, département ou ville.
     *
     * @param query terme de recherche (ex: "Finance", "Martin", "Paris")
     * @return liste des employés correspondants ou message si aucun résultat
     */
    @Tool(description = "Recherche des employés dans l'annuaire interne par nom, département ou ville. Retourne les informations de contact des employés correspondants.")
    public String searchEmployeeDirectory(String query) {
        log.info("[MCP][EmployeeDirectoryTool] Recherche : '{}'", query);

        if (query == null || query.isBlank()) {
            return "Veuillez fournir un terme de recherche (nom, département ou ville).";
        }

        String normalizedQuery = query.trim().toLowerCase();

        List<Employee> results = employees.stream()
                .filter(e -> e.nom().toLowerCase().contains(normalizedQuery)
                        || e.departement().toLowerCase().contains(normalizedQuery)
                        || e.ville().toLowerCase().contains(normalizedQuery))
                .toList();

        String output;
        if (results.isEmpty()) {
            output = "Aucun employé trouvé pour la recherche : '" + query + "'.";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Résultats de la recherche '").append(query).append("' (").append(results.size()).append(" résultat(s)) :\n");
            for (Employee e : results) {
                sb.append(String.format("  • %s — %s (ville : %s) — %s\n",
                        e.nom(), e.departement(), e.ville(), e.email()));
            }
            output = sb.toString().trim();
        }

        return sanitizer.sanitize(output, "EmployeeDirectoryTool");
    }

    /** Représentation d'un employé. */
    private record Employee(String id, String nom, String departement, String ville, String email) {}
}
