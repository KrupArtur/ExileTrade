package com.demo.poe.Model.Json.Filters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {
    private String id;
    private String text;
    private Boolean fullSpan;
    private FilterOption option;

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

    public Boolean getFullSpan() {
        return fullSpan;
    }

    public void setFullSpan(Boolean fullSpan) {
        this.fullSpan = fullSpan;
    }

    public FilterOption getOption() {
        return option;
    }

    public void setOption(FilterOption option) {
        this.option = option;
    }
}
