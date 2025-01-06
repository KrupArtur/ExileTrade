package com.demo.poe.Model.POE.Json.Stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Option {
    @JsonProperty("options")
    private List<OptionEntry> options; // Lista opcji w polu "options"

    public List<OptionEntry> getOptions() {
        return options;
    }

    public void setOptions(List<OptionEntry> options) {
        this.options = options;
    }

}
