package com.example.property;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class InfraClient {
    private final AppConfig config;
    private final HttpClient http = HttpClient.newHttpClient();

    public InfraClient(AppConfig config) { this.config = config; }

    public ChatResult summarize(String input) throws IOException, InterruptedException {
        String body = "{\"model\":\"auto\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escape(input) + "\"}]}";
        String response = send("/chat/completions", body);
        int tokens = extractInt(response, "total_tokens");
        return new ChatResult(response, tokens);
    }

    public void capture(Exception exception, int tokenCount) throws IOException, InterruptedException {
        String body = "{\"exception\":{\"type\":\"" + escape(exception.getClass().getName()) + "\",\"message\":\"" + escape(exception.getMessage()) + "\"},\"context\":{\"token_count\":" + tokenCount + "}}";
        send("/errors/capture", body);
    }

    private String send(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (envelope.contains("\"ok\":false")) {
            throw new IllegalStateException("Infrai request rejected: " + envelope);
        }
        if (response.statusCode() >= 500) {
            throw new IOException("Infrai transport failure: " + response.statusCode());
        }
        return envelope;
    }

    private static int extractInt(String json, String field) {
        String marker = "\"" + field + "\":";
        int start = json.indexOf(marker);
        if (start < 0) return 0;
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Integer.parseInt(json.substring(start, end));
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    public record ChatResult(String rawResponse, int tokenCount) {}
}
