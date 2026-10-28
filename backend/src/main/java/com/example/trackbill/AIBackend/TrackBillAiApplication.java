package com.example.trackbill.AIBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class TrackBillAiApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(TrackBillAiApplication.class, args);
    }

    private static void loadDotEnv() {
        String[] candidatePaths = {
            ".env",
            "backend/.env",
            "../.env",
            System.getProperty("user.dir") + "/.env",
            System.getProperty("user.dir") + "/backend/.env"
        };
        for (String path : candidatePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(envFile.toPath());
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }
}
