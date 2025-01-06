package com.demo.poe.Model.POE.Json.Filters;

import com.demo.poe.Service.TempFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Filters {
    private static Filters instance;
    private static final String nameFileTemp = "filterResponseTempPOE.json";

    public static Filters getInstance(){
        if(instance == null){
            loadData();
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
                            instance = objectMapper.readValue(body, Filters.class);
                            TempFile.saveTempFile(nameFileTemp,body);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
