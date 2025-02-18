package com.demo.poe.Model.POE.Json.Filters;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class FiltersRespons {
    private static FiltersRespons instance;
    private static final String nameFileTemp = "filterResponseTempPOE.json";
    private static final Logger logger = Logger.getLogger(FiltersRespons.class.getName());

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
                            TempFile.saveTempFile(nameFileTemp, body);
                            logger.info("Data loaded from request and saved to temp file");
                        } catch (JsonProcessingException e) {
                            logger.log(Level.SEVERE, "Error processing JSON response", e);
                        }
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error during HTTP request", ex);
                        return null;
                    });
            response.join();
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Invalid URI", e);
        }
    }

    public static void loadDataFromTempFile() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            instance = mapper.readValue(TempFile.readTempFile(nameFileTemp), FiltersRespons.class);
            logger.info("Data loaded from temp file");
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error processing JSON from temp file", e);
        }
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }
}
