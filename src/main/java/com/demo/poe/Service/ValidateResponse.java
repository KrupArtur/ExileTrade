package com.demo.poe.Service;

import javafx.scene.control.Label;

import java.net.http.HttpResponse;

public class ValidateResponse {
    public static String getBodyWithLimit(HttpResponse response, Label resultNotFound) {
        System.out.println("Remaining rate limit: " + response.headers()
                .firstValue("x-rate-limit-rules")
                .orElse("Unknown"));

        System.out.println("Remaining rate limit: " + response.headers()
                .firstValue("X-RateLimit-Reset")
                .orElse("Unknown"));

        System.out.println("limit per ip:" +  response.headers()
                .firstValue("x-rate-limit-" + response.headers()
                        .firstValue("x-rate-limit-rules")
                        .orElse("Unknown"))
                .orElse("Unknown"));

        System.out.println("limit per ip:" +  response.headers()
                .firstValue("x-rate-limit-" + response.headers()
                        .firstValue("x-rate-limit-rules")
                        .orElse("Unknown") + "-state")
                .orElse("Unknown"));
        String responseBody = String.valueOf(response.body());

        if(response.statusCode() == 429){
            RateLimitHandler rateLimitHandler = new RateLimitHandler(resultNotFound);
            rateLimitHandler.handleRateLimitExceeded(Integer.parseInt(response.headers()
                    .firstValue("Retry-After")
                    .orElse("0")));
            return "";
        } else if (response.statusCode() == 200) {
            return responseBody;
        }
        return "";
    }
}
