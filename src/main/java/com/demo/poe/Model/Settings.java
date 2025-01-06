package com.demo.poe.Model;

import java.util.HashMap;
import java.util.Map;

public class Settings {

    private static Map<String ,Settings> instance = new HashMap<>();

    String id;
    String value;

    public Settings() {
    }

    public Settings(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public static Map<String ,Settings> getInstance() {
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
        instance.put(id ,new Settings(id, value));
    }

    public String getValueForPoe(){
        if(!Boolean.parseBoolean(instance.get("exactValuePoE2").getValue())){
            return instance.get("fillStatAroundPoE2").getValue();
        }
        return "";
    }
}
