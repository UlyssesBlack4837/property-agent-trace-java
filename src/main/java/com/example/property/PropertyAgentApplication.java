package com.example.property;

public final class PropertyAgentApplication {
    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        PropertyAgentService service = new PropertyAgentService(new InfraClient(config));
        MaintenanceRequest request = new MaintenanceRequest("unit-204", "Water leak under kitchen sink", "tenant-17");
        System.out.println(service.handle(request));
    }
}
