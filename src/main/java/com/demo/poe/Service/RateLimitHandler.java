package com.demo.poe.Service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class RateLimitHandler {
    private Label resultNotFound;
    private int retryAfter;

    public RateLimitHandler(Label resultNotFound) {
        this.resultNotFound = resultNotFound;
    }

    public void handleRateLimitExceeded(int retryAfter) {
        this.retryAfter = retryAfter;
        startCountdown();
    }

    private void startCountdown() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateCountdown())
        );
        timeline.setCycleCount(retryAfter);
        timeline.play();
    }

    private void updateCountdown() {
        if (retryAfter > 0) {
            Platform.runLater(() -> resultNotFound.setText("Limit Requests Exceeded.\nPlease wait "  + retryAfter + " seconds before trying again"));
            retryAfter--;
        } else {
            Platform.runLater(() -> resultNotFound.setText(""));
        }
    }
}
