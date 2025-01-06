package com.demo.poe.Model.POE.Json.Filters;

public class Options {
    String id;
    String text;

    public Options() {
    }

    public Options(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Options{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
