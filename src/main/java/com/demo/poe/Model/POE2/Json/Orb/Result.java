package com.demo.poe.Model.POE2.Json.Orb;

import java.util.List;
import java.util.Map;

public class Result {
    private String id;
    String complexity;
    String total;
    private Map<String, Orb> result;

    public Result() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public Map<String, Orb> getResult() {
        return result;
    }

    public void setResult(Map<String, Orb> result) {
        this.result = result;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
