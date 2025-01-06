package com.demo.poe.Model.POE2;

public class Mod {
    String id;
    String name;
    String type;
    String value;
    String valueMin;
    String valueMax;

    public Mod() {
    }

    public Mod(String id, String name, String type, String value, String valueMin, String valueMax) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
    }

    public Mod(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Mod(String name, String valueMin, String valueMax) {
        this.name = name;
        this.valueMin = valueMin;
        this.valueMax = valueMax;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueMin() {
        return valueMin;
    }

    public void setValueMin(String valueMin) {
        this.valueMin = valueMin;
    }

    public String getValueMax() {
        return valueMax;
    }

    public void setValueMax(String valueMax) {
        this.valueMax = valueMax;
    }

    @Override
    public String toString() {
        return "Mod{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", valueMin='" + valueMin + '\'' +
                ", valueMax='" + valueMax + '\'' +
                '}';
    }
}
