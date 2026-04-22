package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DemoApplication.class);
        application.setDefaultProperties(loadDotenvDefaults());
        application.run(args);
    }

    private static Map<String, Object> loadDotenvDefaults() {
        Path dotenvPath = Path.of(".env");
        if (!Files.exists(dotenvPath)) {
            return Map.of();
        }

        Map<String, Object> defaults = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(dotenvPath, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();
                switch (key) {
                    case "SPRING_DATASOURCE_URL" -> defaults.put("spring.datasource.url", value);
                    case "SPRING_DATASOURCE_USERNAME" -> defaults.put("spring.datasource.username", value);
                    case "SPRING_DATASOURCE_PASSWORD" -> defaults.put("spring.datasource.password", value);
                    default -> {
                    }
                }
            }
        } catch (IOException exception) {
            System.err.println("Failed to read .env defaults: " + exception.getMessage());
            return Map.of();
        }

        return defaults;
    }

}
