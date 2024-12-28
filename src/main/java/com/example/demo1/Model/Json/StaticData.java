package com.example.demo1.Model.Json;
import com.example.demo1.Service.TempFile;
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

public class StaticData {
    private static StaticData instance;

    @JsonProperty("result")
    private List<StaticDataResult> results;

    public StaticData() {}

    public static StaticData getInstance() {
        if (instance == null) {
            instance = new StaticData();
            if(!TempFile.fileTempExists()){
                loadDataFromRequest();
            } else {
                loadDataFromTempFile();
            }

        }
        return instance;
    }

    public static void createInstance(){
        if (instance == null) {
            instance = new StaticData();
            if(!TempFile.fileTempExists()){
                loadDataFromRequest();
            } else {
                loadDataFromTempFile();
            }
        }
    }

    private static void loadDataFromRequest() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.pathofexile.com/api/trade2/data/stats"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response.thenApply(HttpResponse::body)
                    .thenAccept(body -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            instance = mapper.readValue(body, StaticData.class);
                            TempFile.saveTempFile("staticDataTemp",body);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    })
                    .exceptionally( ex ->{
                        System.err.println(ex);
                        return null;
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public static void loadDataFromTempFile(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            instance = mapper.readValue(TempFile.readTempFile(), StaticData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    public List<StaticDataResult> getResults() {
        return results;
    }

    public void setResults(List<StaticDataResult> results) {
        this.results = results;
    }
}

