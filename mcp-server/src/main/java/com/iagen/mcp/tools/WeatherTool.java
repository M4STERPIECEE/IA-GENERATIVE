package com.iagen.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iagen.mcp.security.OutputSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Outil MCP — Domaine WEB/API.
 * Récupère la météo actuelle d'une ville via l'API open-meteo (sans clé API).
 */
@Service
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    private static final String GEO_API = "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=fr&format=json";
    private static final String WEATHER_API = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&wind_speed_unit=kmh&timezone=auto";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OutputSanitizer sanitizer;

    public WeatherTool(OutputSanitizer sanitizer) {
        this.sanitizer = sanitizer;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retourne la météo actuelle d'une ville.
     *
     * @param city le nom de la ville (ex: "Paris", "Lyon", "Marseille")
     * @return description texte de la météo actuelle
     */
    @Tool(description = "Récupère la météo actuelle d'une ville. Utiliser pour toute question sur le temps, la température, ou la météo d'un lieu.")
    public String getCurrentWeather(String city) {
        log.info("[MCP][WeatherTool] Météo demandée pour : {}", city);
        try {
            // Étape 1 : géocodage ville → lat/lon
            String geoUrl = String.format(GEO_API, city.trim().replace(" ", "+"));
            JsonNode geoResponse = fetchJson(geoUrl);

            JsonNode results = geoResponse.path("results");
            if (results.isEmpty()) {
                return sanitizer.sanitize("Ville introuvable : " + city, "WeatherTool");
            }

            JsonNode location = results.get(0);
            double lat = location.path("latitude").asDouble();
            double lon = location.path("longitude").asDouble();
            String countryCode = location.path("country_code").asText("");

            // Étape 2 : météo actuelle
            String weatherUrl = String.format(WEATHER_API, lat, lon);
            JsonNode weatherResponse = fetchJson(weatherUrl);

            JsonNode current = weatherResponse.path("current");
            double temp = current.path("temperature_2m").asDouble();
            int humidity = current.path("relative_humidity_2m").asInt();
            double windSpeed = current.path("wind_speed_10m").asDouble();
            int weatherCode = current.path("weather_code").asInt();
            String description = decodeWeatherCode(weatherCode);

            String result = String.format(
                    "Météo actuelle à %s (%s) : %s. Température : %.1f°C, Humidité : %d%%, Vent : %.1f km/h.",
                    location.path("name").asText(city), countryCode, description, temp, humidity, windSpeed
            );

            return sanitizer.sanitize(result, "WeatherTool");

        } catch (Exception e) {
            log.error("[MCP][WeatherTool] Erreur lors de la récupération météo pour {} : {}", city, e.getMessage());
            return "Impossible de récupérer la météo pour '" + city + "' (erreur réseau).";
        }
    }

    private JsonNode fetchJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    /** Convertit le code météo WMO en description lisible. */
    private String decodeWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Ciel dégagé";
            case 1 -> "Principalement dégagé";
            case 2 -> "Partiellement nuageux";
            case 3 -> "Couvert";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine légère";
            case 61, 63, 65 -> "Pluie";
            case 71, 73, 75 -> "Neige";
            case 80, 81, 82 -> "Averses de pluie";
            case 95 -> "Orage";
            case 96, 99 -> "Orage avec grêle";
            default -> "Conditions météo inconnues (code " + code + ")";
        };
    }
}
