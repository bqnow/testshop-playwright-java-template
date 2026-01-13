package com.bqnow.testshop.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Konfigurations-Loader mit Unterstützung für umgebungsspezifische Dateien.
 * Priorität: 1. System/Env Variablen, 2. .env.local, 3. .env.{TEST_ENV}, 4.
 * .env
 */
public class ConfigLoader {
    private static ConfigLoader instance;
    private final Dotenv dotenv;
    private final String baseURL;
    private final String testUserName;
    private final String testUserPassword;
    private final boolean isCI;
    private final boolean skipWebkit;

    private ConfigLoader() {
        // Umgebungsspezifische Konfiguration laden
        String testEnv = System.getenv("TEST_ENV");
        Path configDir = Paths.get("config");

        // Bestimmen, welche .env Datei basierend auf Priorität geladen wird
        String envFile = ".env";
        if (Files.exists(configDir.resolve(".env.local"))) {
            envFile = ".env.local";
        } else if (testEnv != null && Files.exists(configDir.resolve(".env." + testEnv))) {
            envFile = ".env." + testEnv;
        }

        this.dotenv = Dotenv.configure()
                .directory(configDir.toString())
                .filename(envFile)
                .ignoreIfMissing()
                .load();

        // Konfigurationswerte laden mit Priorität: System Env > .env Dateien
        this.baseURL = getEnvOrDefault("BASE_URL", "http://localhost:3000");
        this.testUserName = getEnvOrDefault("TEST_USER_NAME", "consultant");
        this.testUserPassword = getEnvOrDefault("TEST_USER_PASSWORD", "pwd");
        this.isCI = "true".equals(getEnvOrDefault("CI", "false"));
        this.skipWebkit = "true".equals(getEnvOrDefault("SKIP_WEBKIT", "false"));
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        // System Env hat höchste Priorität
        String sysEnv = System.getenv(key);
        if (sysEnv != null) {
            return sysEnv;
        }
        // Dann Dotenv
        return dotenv.get(key, defaultValue);
    }

    public String getBaseURL() {
        return baseURL;
    }

    public String getTestUserName() {
        return testUserName;
    }

    public String getTestUserPassword() {
        return testUserPassword;
    }

    public boolean isCI() {
        return isCI;
    }

    public boolean skipWebkit() {
        return skipWebkit;
    }

    public boolean isHeadless() {
        return !"false".equals(getEnvOrDefault("HEADLESS", "false"));
    }
}
