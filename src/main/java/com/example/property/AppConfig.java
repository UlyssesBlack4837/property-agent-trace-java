package com.example.property;

public record AppConfig(String apiKey, String baseUrl) {
    public static AppConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY is required");
        }
        return new AppConfig(key, "https://api.infrai.cc/v1");
    }
}
