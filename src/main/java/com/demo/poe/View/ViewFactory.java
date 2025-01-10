package com.demo.poe.View;

import com.demo.poe.Controller.OptionsWindowController;
import com.demo.poe.Service.Events.KeyEventHandler;
import com.demo.poe.HelloApplication;
import com.demo.poe.Model.Json.Orb.OrbsResons;
import com.demo.poe.PoeTradeManager;
import com.demo.poe.Service.SettingsManager;
import com.demo.poe.Controller.MainWindowController;
import com.demo.poe.Controller.BaseController;
import com.demo.poe.Service.WindowDetector;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import javafx.stage.WindowEvent;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ViewFactory {
    private PoeTradeManager poeTradeManager;
    public Map<String,Stage> activeStages;

    public ViewFactory(PoeTradeManager poeTradeManager) {
        this.poeTradeManager = poeTradeManager;
        activeStages = new HashMap<>();
    }

    public void showMainWindow() {
        BaseController controller = new MainWindowController(poeTradeManager, this, "MainWindow.fxml");
        initializeStage(controller);
    }

    public void showOptionsWindow(){
        BaseController controller = new OptionsWindowController(poeTradeManager, this, "OptionsWindow.fxml");
        initializeStage(controller);
    }

    private void initializeStage(BaseController baseController){
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(baseController.getFxmlName()));
        fxmlLoader.setController(baseController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Objects.requireNonNull(HelloApplication.class.getResource("main.css")).toExternalForm());

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Stage stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(SettingsManager.getInstance().getSetting("icon32")))));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(SettingsManager.getInstance().getSetting("title"));

        stage.setOpacity(0.9);
        stage.setScene(scene);

        baseController.setStage(stage);

        if(fxmlLoader.getController() instanceof MainWindowController mainWindowController){
            stage.show();
            stage.hide();

            stage.setOnCloseRequest(event -> {
                event.consume();
                hidenStage(stage);
            });
            activeStages.put("MainWindow", stage);

            stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
                OrbsResons.getInstance(mainWindowController.getPriceDivine(), WindowDetector.getGameWindow("Path of Exile") != null);
            });

            try {
                GlobalScreen.registerNativeHook();
                GlobalScreen.addNativeKeyListener(new KeyEventHandler(stage, mainWindowController));
            } catch (NativeHookException e) {
                e.printStackTrace();
            }

        } else if (fxmlLoader.getController() instanceof  OptionsWindowController){

            activeStages.put("OptionsWindow", stage);
            stage.show();
        }
    }

    public void closeStage(Stage stageToClose){
        Platform.runLater(stageToClose::close);
    }

    public void hidenStage(Stage stageToHiden){
        stageToHiden.hide();
    }

    public Stage getStage(String stage){
        return activeStages.get(stage);
    }

    public void removeStage(String stage){
        activeStages.remove(stage);
    }

    public Stage getMainWindow(){
        return getStage("MainWindow");
    }



}
