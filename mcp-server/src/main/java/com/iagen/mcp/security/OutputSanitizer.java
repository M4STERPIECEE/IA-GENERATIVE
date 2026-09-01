package com.iagen.mcp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class OutputSanitizer {

    private static final Logger log = LoggerFactory.getLogger(OutputSanitizer.class);

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore(z)?\\s+(toutes\\s+)?les\\s+instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ignore\\s+previous\\s+instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("forget\\s+(all\\s+)?.*instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("system\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("tu\\s+es\\s+maintenant", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\s*system\\s*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\[SYSTEM\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("act\\s+as\\s+(a\\s+|an\\s+)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("DAN\\s+mode", Pattern.CASE_INSENSITIVE)
    );

    private static final String REPLACEMENT = "[CONTENU_NEUTRALISE]";

    public String sanitize(String toolOutput, String toolName) {
        if (toolOutput == null || toolOutput.isBlank()) {
            return toolOutput;
        }

        String sanitized = toolOutput;
        boolean injectionDetected = false;

        for (Pattern pattern : INJECTION_PATTERNS) {
            String cleaned = pattern.matcher(sanitized).replaceAll(REPLACEMENT);
            if (!cleaned.equals(sanitized)) {
                injectionDetected = true;
                sanitized = cleaned;
            }
        }

        if (injectionDetected) {
            log.warn("[SECURITY][MCP-OUTPUT] Injection de prompt détectée dans la sortie de l'outil '{}'. Contenu neutralisé.", toolName);
        }

        return sanitized;
    }
}
