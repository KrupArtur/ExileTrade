package com.demo.poe.Model;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private static List<Settings> instance = new ArrayList<>();

    String id;
    String value;

    public Settings() {
    }

    public Settings(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public static List<Settings> getInstance() {
        return instance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static void addSetting(String id, String value) {
        instance.add(new Settings(id, value));
    }
}
