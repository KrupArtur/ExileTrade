package com.demo.poe.Model.Json.Stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Option {
    private String value;
    private String description;

    @JsonProperty("options")
    private Object options;

    // Gettery i settery
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }
}
