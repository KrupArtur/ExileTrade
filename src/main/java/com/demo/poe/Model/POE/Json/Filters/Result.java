package com.demo.poe.Model.POE.Json.Filters;

import java.util.List;

public class Result {
    String id;
    String title;
    List<Filters> filters;
    boolean hidden;

    public Result() {
    }

    public Result(String id, String title, List<Filters> filters, boolean hidden) {
        this.id = id;
        this.title = title;
        this.filters = filters;
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Filters> getFilters() {
        return filters;
    }

    public void setFilters(List<Filters> filters) {
        this.filters = filters;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
