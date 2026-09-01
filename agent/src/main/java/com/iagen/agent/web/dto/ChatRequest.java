package com.iagen.agent.web.dto;

/**
 * Requête entrante sur l'endpoint POST /api/chat.
 *
 * @param question la question posée par l'utilisateur
 */
public record ChatRequest(String question) {}
