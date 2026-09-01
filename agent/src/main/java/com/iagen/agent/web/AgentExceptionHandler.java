package com.iagen.agent.web;

import com.iagen.agent.web.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class AgentExceptionHandler {


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ChatResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Requête mal formattée : {}", ex.getMessage());
        return buildErrorResponse("Format de requête invalide. Veuillez envoyer un JSON valide",
                HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleGlobalException(Exception ex) {
        log.error("Erreur système inattendue : ", ex);
        return buildErrorResponse("Une erreur interne inattendue est survenue au niveau du serveur",
                HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_ERROR");
    }

    private ResponseEntity<ChatResponse> buildErrorResponse(String message, HttpStatus status, String route) {
        String traceEntry = Instant.now().toString() + " [SYSTEM] " + message;

        ChatResponse response = ChatResponse.builder()
                .answer(message)
                .route(route)
                .reasoning("Erreur attrapée par le GlobalExceptionHandler")
                .sources(List.of())
                .trace(List.of(traceEntry))
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
