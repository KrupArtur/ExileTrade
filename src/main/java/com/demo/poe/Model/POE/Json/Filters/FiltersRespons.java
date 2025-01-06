package com.demo.poe.Model.POE.Json.Filters;

import com.demo.poe.Model.POE.Json.Stats.StatsRespons;
import com.demo.poe.Service.TempFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FiltersRespons {
    private static FiltersRespons instance;
    private static final String nameFileTemp = "filterResponseTempPOE.json";

    List<Result> result;

    public static FiltersRespons getInstance(){
        if(instance == null){
            if(!TempFile.fileTempExists(nameFileTemp)){
                loadData();
            } else {
                loadDataFromTempFile();
            }

        } 
        return instance;        
    }

    private static void loadData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.pathofexile.com/api/trade/data/filters"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response.thenApply(HttpResponse::body)
                    .thenAccept(body -> {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            instance = objectMapper.readValue(body, FiltersRespons.class);
                            TempFile.saveTempFile(nameFileTemp,body);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void loadDataFromTempFile(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            instance = mapper.readValue(TempFile.readTempFile(nameFileTemp), FiltersRespons.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }
}
