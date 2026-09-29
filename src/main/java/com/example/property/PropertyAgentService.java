package com.example.property;

public final class PropertyAgentService {
    private final InfraClient client;
    public PropertyAgentService(InfraClient client) { this.client = client; }

    public String handle(MaintenanceRequest request) {
        int tokens = 0;
        try {
            InfraClient.ChatResult result = client.summarize("Classify this maintenance request for " + request.unit() + ": " + request.description());
            tokens = result.tokenCount();
            return "inspection-reminder: " + request.unit() + " | tenant-document=" + request.tenantId() + " | tokens=" + tokens;
        } catch (Exception failure) {
            try { client.capture(failure, tokens); } catch (Exception ignored) { }
            return "inspection-reminder: " + request.unit() + " | status=manual-review | tokens=" + tokens;
        }
    }
}
