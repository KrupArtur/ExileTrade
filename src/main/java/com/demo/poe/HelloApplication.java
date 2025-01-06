package com.demo.poe;

import com.demo.poe.Model.POE.Json.Filters.FiltersRespons;
import com.demo.poe.Model.POE.Json.Stats.StatsRespons;
import com.demo.poe.Model.POE2.Json.Filters.FilterResponse;
import com.demo.poe.Model.POE2.Json.Stats.StaticData;
import com.demo.poe.Service.SettingsManager;
import com.demo.poe.View.ViewFactory;
import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;
import com.dustinredmond.fxtrayicon.*;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        Logger logger = Logger.getLogger(org.jnativehook.GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.SEVERE);
        FiltersRespons.getInstance();
        StatsRespons.getInstance();
        StaticData.createInstance();
        FilterResponse.createInstance();
        ViewFactory viewFactory = new ViewFactory(new PoeTradeManager());
        viewFactory.showMainWindow();

        new FXTrayIcon.Builder(viewFactory.getMainWindow(), getClass().getResource(SettingsManager.getInstance().getSetting("icon32")))
                .menuItem("Show", e -> {
                    viewFactory.getMainWindow().show();
                }).menuItem("Settings",e -> {
                   viewFactory.getMainWindow().hide();
                   viewFactory.showOptionsWindow();
                })
                .addExitMenuItem("Exit", e ->{
                    Platform.exit();
                    System.exit(0);
                })
                .show()
                .build();
    }
}