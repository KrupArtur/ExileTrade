package com.demo.poe.Service;

import com.demo.poe.HelloApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class SettingsManager {
    private static SettingsManager instance;
    private JsonNode settingsNode;
    private final String settingsFile = "com/demo/poe/config/settings.json";

    public SettingsManager() {
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public void loadSettings() {
        try (InputStream inputStream = HelloApplication.class.getResourceAsStream("config/settings.json")) {
            if (inputStream == null) {
                throw new IOException("Plik settings.json nie został znaleziony.");
            }

            ObjectMapper mapper = new ObjectMapper();
            settingsNode = mapper.readTree(inputStream);
        } catch (IOException e) {
            System.out.println("Nie udało się wczytać ustawień: " + e.getMessage());
        }
    }

    public String getSetting(String key) {
        return settingsNode.path("app").path(key).asText();
    }

}
