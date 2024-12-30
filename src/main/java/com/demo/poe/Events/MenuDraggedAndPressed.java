package com.demo.poe.Events;

import com.demo.poe.Service.WindowDetector;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MenuDraggedAndPressed {
    private double xOffset = 0;
    private double yOffset = 0;
    WinDef.RECT gameWindowRect;
    public void menuDragged(MouseEvent mouseEvent){
        if(gameWindowRect != null) {
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            double newX = mouseEvent.getScreenX() - xOffset;
            double newY = mouseEvent.getScreenY() - yOffset;

            if (newX < gameWindowRect.left) {
                newX = gameWindowRect.left;
            }
            if (newX + stage.getWidth() > gameWindowRect.right) {
                newX = gameWindowRect.right - stage.getWidth();
            }
            if (newY < gameWindowRect.top) {
                newY = gameWindowRect.top;
            }
            if (newY + stage.getHeight() > gameWindowRect.bottom) {
                newY = gameWindowRect.bottom - stage.getHeight();
            }

            stage.setX(newX);
            stage.setY(newY);
        }
    }

    public void menuPressed(MouseEvent mouseEvent) {
        WinDef.RECT gameWindowRect = WindowDetector.getGameWindow("Path of Exile 2");
        if (gameWindowRect != null) {
            xOffset = mouseEvent.getSceneX();
            yOffset = mouseEvent.getSceneY();
            this.gameWindowRect = gameWindowRect;
        }
    }
}
