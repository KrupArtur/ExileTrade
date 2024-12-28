package com.demo.poe.Model.Json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultForQuery {

    private static ResultForQuery instance;

    private String id;
    private int complexity;
    @JsonProperty("result")
    private List<String> result;

    private ResultForQuery() {}


    public static ResultForQuery getInstance() {
        if (instance == null) {
            instance = new ResultForQuery();
        }
        return instance;
    }

    public static void loadDataFromRequest(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            instance = objectMapper.readValue(body, ResultForQuery.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gettery i settery
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id='" + id + '\'' +
                ", complexity=" + complexity +
                ", result=" + result +
                '}';
    }
}
