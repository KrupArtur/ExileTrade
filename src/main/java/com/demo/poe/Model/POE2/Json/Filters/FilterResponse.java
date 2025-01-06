package com.demo.poe.Model.POE2.Json.Filters;

import com.demo.poe.Service.TempFile;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FilterResponse {
    private static FilterResponse instance;
    private static final String nameFileTemp = "filterResponseTempPOE2.json";
    @JsonProperty("result")
    List<FilterType> filters;

    public FilterResponse() {
    }

    public static void createInstance(){
        if (instance == null) {
            if(!TempFile.fileTempExists(nameFileTemp)){
                loadDataFromRequest();
            } else {
                loadDataFromTempFile();
            }
        }
    }

    public static FilterResponse getInstance() {
        if (instance == null) {
            instance = new FilterResponse();
            if(!TempFile.fileTempExists(nameFileTemp)){
                loadDataFromRequest();
            } else {
                loadDataFromTempFile();
            }

        }
        return instance;
    }
    public static void loadDataFromRequest() {
        try {
            HttpRequest request  = HttpRequest.newBuilder()
                    .uri(new URI("https://www.pathofexile.com/api/trade2/data/filters"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

        CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
        response.thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        instance = mapper.readValue(body, FilterResponse.class);
                        TempFile.saveTempFile(nameFileTemp,body);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(ex -> {
                    System.err.println(ex);
                    return null;
                });
        response.join();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void loadDataFromTempFile(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            instance = mapper.readValue(TempFile.readTempFile(nameFileTemp), FilterResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<FilterType> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterType> filters) {
        this.filters = filters;
    }
}
