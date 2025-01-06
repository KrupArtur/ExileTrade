package com.demo.poe.Model.POE.Json.Stats;

import java.util.List;

public class Result {
    String id;
    String label;
    List<Entries> entries;

    public Result() {
    }

    public Result(String id, String label, List<Entries> entries) {
        this.id = id;
        this.label = label;
        this.entries = entries;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Entries> getEntries() {
        return entries;
    }

    public void setEntries(List<Entries> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "Result{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", entries=" + entries +
                '}';
    }
}
