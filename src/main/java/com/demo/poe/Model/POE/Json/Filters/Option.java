package com.demo.poe.Model.POE.Json.Filters;

import java.util.List;

public class Option {
    List<Options> options;
    KnownItem knownItem;

    public Option() {
    }

    public Option(List<Options> options, KnownItem knownItem) {
        this.options = options;
        this.knownItem = knownItem;
    }

    public List<Options> getOptions() {
        return options;
    }

    public void setOptions(List<Options> options) {
        this.options = options;
    }

    public KnownItem getKnownItem() {
        return knownItem;
    }

    public void setKnownItem(KnownItem knownItem) {
        this.knownItem = knownItem;
    }
}
