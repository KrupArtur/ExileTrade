package com.demo.poe.Model.Json.Orb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OrbsResons {
    private static final String urlPOE = "https://www.pathofexile.com/api/trade/exchange/Standard";
    private static final String urlPOE2 = "https://www.pathofexile.com/api/trade2/exchange/poe2/Standard";
    private static long scheduledTime = -1;
    public static double divine = 0;

    public static void getInstance(Label label,boolean isPOE){
           // loadData(label, isPOE);
    }

    private static void loadData(Label label, boolean isPOE) {
         try {
             if(scheduledTime - System.currentTimeMillis() >= 240000 || divine == 0) {
                 String query = "{\"query\":{\"status\":{\"option\":\"online\"},\"have\":[\"exalted\"],\"want\":[\"divine\"]},\"sort\":{\"have\":\"asc\"},\"engine\":\"new\"}";

                 HttpRequest httpRequest = HttpRequest.newBuilder()
                         .uri(new URI(isPOE ? urlPOE : urlPOE2))
                         .header("Content-Type", "application/json")
                         .POST(HttpRequest.BodyPublishers.ofString(query))
                         .build();

                 CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
                 response.thenApply(HttpResponse::body)
                         .thenAccept(body -> {
                             try {
                                 ObjectMapper mapper = new ObjectMapper();
                                 Platform.runLater(() -> {
                                     try {
                                         label.setText("x 1 ⇐ " + getPriceOrb(mapper.readValue(body, Result.class)) + " x");
                                     } catch (JsonProcessingException e) {
                                         e.printStackTrace();
                                     }
                                 });
                                 scheduledTime = System.currentTimeMillis();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                         });
             }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static double getPriceOrb(Result result){
        int count = 0;
        double avg = 0.0;
        for (String key: result.getResult().keySet()) {
            Orb orb = result.getResult().get(key);
            Listing listing = orb.getListing();
            List<Offer> offersList = listing.getOffers();
            for (Offer offers: offersList) {
                if(count >= 20) break;

                avg +=  offers.getExchange().getAmount() / offers.getItem().getAmount();

                count++;
            }
            if(count >= 20) break;
        }
        return avg / 20;
    }

}
