package com.demo.poe.Model.POE2.Json.Stats;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticData {
    private static StaticData instance;
    private static final String nameFileTemp = "staticDataTempPOE2.json";
    private static final Logger logger = Logger.getLogger(StaticData.class.getName());

    @JsonProperty("result")
    private List<StaticDataResult> results;

    public StaticData() {}

    public static StaticData getInstance() {
        if (instance == null) {
            instance = new StaticData();
           if(!TempFile.fileTempExists(nameFileTemp)){
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
            if(!TempFile.fileTempExists(nameFileTemp)){
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
            instance = mapper.readValue(TempFile.readTempFile(nameFileTemp), StaticData.class);
            logger.info("Data loaded from temp file");
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error processing JSON from temp file", e);
        }
    }

    public List<StaticDataResult> getResults() {
        return results;
    }

    public void setResults(List<StaticDataResult> results) {
        this.results = results;
    }

}

