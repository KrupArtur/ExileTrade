package com.demo.poe.Model.POE.Json.Filters;

public class Filters {
    String id;
    String text;
    String fullSpan;
    Option option;
    boolean minMax;
    String tip;
    String sockets;
    Input input;

    public Filters() {
    }

    public Filters(String id, String text, String fullSpan, Option option, boolean minMax, String tip, String sockets, Input input) {
        this.id = id;
        this.text = text;
        this.fullSpan = fullSpan;
        this.option = option;
        this.minMax = minMax;
        this.tip = tip;
        this.sockets = sockets;
        this.input = input;
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

    public String getFullSpan() {
        return fullSpan;
    }

    public void setFullSpan(String fullSpan) {
        this.fullSpan = fullSpan;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public boolean isMinMax() {
        return minMax;
    }

    public void setMinMax(boolean minMax) {
        this.minMax = minMax;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getSockets() {
        return sockets;
    }

    public void setSockets(String sockets) {
        this.sockets = sockets;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }
}
