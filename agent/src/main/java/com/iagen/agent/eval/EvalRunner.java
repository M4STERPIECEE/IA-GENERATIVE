package com.iagen.agent.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iagen.agent.orchestration.OrchestratorService;
import com.iagen.agent.orchestration.TraceCollector;
import com.iagen.agent.routing.RoutingDecision;
import com.iagen.agent.routing.RouterService;
import com.iagen.agent.web.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("eval")
@Order(10)
@Slf4j
@RequiredArgsConstructor
public class EvalRunner implements ApplicationRunner {

    private static final String QUESTIONS_PATH = "eval/questions.json";
    private static final String REPORT_PATH = "eval/report.md";

    private final RouterService routerService;
    private final OrchestratorService orchestratorService;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[EVAL] Démarrage de l'évaluation automatique...");

        File questionsFile = new File(QUESTIONS_PATH);
        if (!questionsFile.exists()) {
            log.error("[EVAL] Fichier questions introuvable : {}", questionsFile.getAbsolutePath());
            return;
        }

        JsonNode root = objectMapper.readTree(questionsFile);
        JsonNode questions = root.path("questions");

        List<EvalResult> results = new ArrayList<>();
        int passed = 0;
        int failed = 0;

        for (JsonNode q : questions) {
            int id = q.path("id").asInt();
            String question = q.path("question").asText();
            String expectedRoute = q.path("expectedRoute").asText();
            List<String> mustContain = new ArrayList<>();
            q.path("mustContain").forEach(n -> mustContain.add(n.asText()));
            String type = q.path("type").asText();

            log.info("[EVAL] Question {} ({}) : {}", id, type, question);

            try {
                TraceCollector trace = new TraceCollector();
                RoutingDecision decision = routerService.route(question);
                ChatResponse response = orchestratorService.orchestrate(question, decision, trace);

                String actualRoute = response.getRoute();
                String answer = response.getAnswer();

                boolean routeOk = actualRoute.equalsIgnoreCase(expectedRoute)
                        || actualRoute.startsWith(expectedRoute);
                boolean contentOk = mustContain.stream()
                        .allMatch(keyword -> answer.toLowerCase().contains(keyword.toLowerCase()));

                boolean pass = routeOk && contentOk;
                if (pass)
                    passed++;
                else
                    failed++;

                results.add(new EvalResult(id, type, question, expectedRoute, actualRoute,
                        routeOk, contentOk, pass, answer));

                log.info("[EVAL] Q{} : {} (route={}, content={})", id, pass ? "PASS ✓" : "FAIL ✗", routeOk, contentOk);

            } catch (Exception e) {
                log.error("[EVAL] Q{} ERREUR : {}", id, e.getMessage());
                results.add(new EvalResult(id, type, question, expectedRoute, "ERROR",
                        false, false, false, "Exception : " + e.getMessage()));
                failed++;
            }
        }

        writeReport(results, passed, failed);

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.printf("║  RAPPORT D'ÉVALUATION iAgen Agent IA             ║%n");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  PASS : %-3d  |  FAIL : %-3d  |  TOTAL : %-3d     ║%n", passed, failed, passed + failed);
        System.out.println("╠══════════════════════════════════════════════════╣");
        for (EvalResult r : results) {
            System.out.printf("║  Q%-2d [%s] %-38s ║%n",
                    r.id(), r.pass() ? "PASS ✓" : "FAIL ✗", "(" + r.type() + ")");
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("Rapport complet : " + new File(REPORT_PATH).getAbsolutePath());

        final int failedCount = failed;
        SpringApplication.exit(applicationContext, () -> failedCount == 0 ? 0 : 1);
    }

    private void writeReport(List<EvalResult> results, int passed, int failed) throws Exception {
        Files.createDirectories(Paths.get("eval"));
        try (PrintWriter pw = new PrintWriter(new FileWriter(REPORT_PATH, StandardCharsets.UTF_8))) {
            pw.println("# Rapport d'évaluation — iAgen Agent IA");
            pw.println();
            pw.printf("**Résumé** : %d/%d tests réussis (%.0f%%)%n",
                    passed, passed + failed, passed * 100.0 / (passed + failed));
            pw.println();
            pw.println("| Q# | Type | Route attendue | Route réelle | Route OK | Contenu OK | Résultat |");
            pw.println("|---|---|---|---|---|---|---|");
            for (EvalResult r : results) {
                pw.printf("| %d | %s | %s | %s | %s | %s | **%s** |%n",
                        r.id(), r.type(), r.expectedRoute(), r.actualRoute(),
                        r.routeOk() ? "✓" : "✗",
                        r.contentOk() ? "✓" : "✗",
                        r.pass() ? "PASS" : "FAIL");
            }
            pw.println();
            pw.println("## Détail des réponses");
            pw.println();
            for (EvalResult r : results) {
                pw.printf("### Q%d [%s] — %s%n", r.id(), r.pass() ? "PASS" : "FAIL", r.type());
                pw.printf("**Question** : %s%n%n", r.question());
                pw.printf("**Réponse** :%n> %s%n%n", r.answer().replace("\n", "\n> "));
                pw.println("---");
            }
        }
        log.info("[EVAL] Rapport écrit → {}", new File(REPORT_PATH).getAbsolutePath());
    }

}
