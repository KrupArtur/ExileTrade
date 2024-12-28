package com.example.demo1.Controller;

import com.example.demo1.PoeTradeManager;
import com.example.demo1.View.ViewFactory;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

public class OptionsWindowController extends BaseController {

    public OptionsWindowController(PoeTradeManager poeTradeManager, ViewFactory viewFactory, String fxmlName) {
        super(poeTradeManager, viewFactory, fxmlName);
    }

    public void menuDragged(MouseEvent mouseEvent) {
    }

    public void menuPressed(MouseEvent mouseEvent) {
    }

    public void closeBtn(ActionEvent actionEvent) {
        viewFactory.getStage("OptionsWindow").close();
        viewFactory.removeStage("OptionsWindow");
        viewFactory.getStage("MainWindow").show();
    }
}
