package com.example.property;

public final class PropertyAgentServiceTest {
    public static void main(String[] args) {
        PropertyAgentService service = new PropertyAgentService(new InfraClient(new AppConfig("test-key", "http://127.0.0.1:1/v1")));
        String result = service.handle(new MaintenanceRequest("unit-9", "broken heater", "tenant-2"));
        if (!result.contains("status=manual-review") || !result.contains("unit-9")) {
            throw new AssertionError("failed requests must become manual review reminders");
        }
        System.out.println("PropertyAgentServiceTest passed");
    }
}
