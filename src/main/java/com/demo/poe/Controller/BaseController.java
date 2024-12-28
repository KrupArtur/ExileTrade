package com.demo.poe.Controller;

import com.demo.poe.PoeTradeManager;
import com.demo.poe.View.ViewFactory;
import javafx.stage.Stage;

public abstract class BaseController {

    protected PoeTradeManager poeTradeManager;
    protected ViewFactory viewFactory;
    protected Stage stage;
    private String fxmlName;

    public BaseController(PoeTradeManager poeTradeManager, ViewFactory viewFactory, String fxmlName) {
        this.poeTradeManager = poeTradeManager;
        this.viewFactory = viewFactory;
        this.fxmlName = fxmlName;
    }

    public String getFxmlName() {
        return fxmlName;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
