package com.demo.poe.Controller;

import com.demo.poe.Service.Events.MenuDraggedAndPressed;
import com.demo.poe.HelloApplication;
import com.demo.poe.Model.POE2.ItemDetails;
import com.demo.poe.Model.POE2.Json.ResultForQuery;
import com.demo.poe.Model.Settings;
import com.demo.poe.PoeTradeManager;
import com.demo.poe.Service.*;
import com.demo.poe.Service.poe.POE;
import com.demo.poe.Service.poe2.POE2;
import com.demo.poe.Service.poe2.QuerySearch;
import com.demo.poe.View.ViewFactory;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.List;

import javafx.scene.Cursor;
import javafx.scene.image.Image;

public class MainWindowController extends BaseController {
    private static final MenuDraggedAndPressed menuDraggedAndPressed = new MenuDraggedAndPressed();
    private Stage stage;

    @FXML
    private CheckBox isCorrupted;

    @FXML
    private TextField itemLevelField;

    @FXML
    private TextField itemQualityField;

    @FXML
    private Button searchBtn;

    @FXML
    private TableColumn<ItemDetails, String> lvl;

    @FXML
    private TableColumn<ItemDetails, String> price;

    @FXML
    private TableView<ItemDetails> table;

    @FXML
    private VBox mods;

    @FXML
    private Label titleApplicationLabel;

    @FXML
    private ImageView iconApplication;

    @FXML
    private Label resultNotFound;

    @FXML
    private ImageView divineOrb;

    @FXML
    private ImageView exileOrb;

    @FXML
    private Label priceDivine;

    private double xOffset = 0;
    private double yOffset = 0;

    public MainWindowController(PoeTradeManager poeTradeManager, ViewFactory viewFactory, String fxmlName) {
        super(poeTradeManager, viewFactory, fxmlName);
    }


    @FXML
    public void initialize() throws IOException {
        lvl.setCellValueFactory(new PropertyValueFactory<>("level"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));

        iconApplication.setImage(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(SettingsManager.getInstance().getSetting("icon32")))));

        divineOrb.setImage(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(
                WindowDetector.getGameWindow("Path of Exile") != null ?
                        SettingsManager.getInstance().getSetting("divinePOE") : SettingsManager.getInstance().getSetting("divine")))));
        exileOrb.setImage(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(
                WindowDetector.getGameWindow("Path of Exile") != null ?
                        SettingsManager.getInstance().getSetting("exilePOE") :SettingsManager.getInstance().getSetting("exile") ))));

        titleApplicationLabel.setText(SettingsManager.getInstance().getSetting("title"));

        ScrollBar verticalScrollBar = getVerticalScrollBar();

        if (verticalScrollBar != null) {
            verticalScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0 && ResultForQuery.getInstance().getResult().size() > 9) {
                    fetchItems();
                }
            });
        }

        itemLevelField.textProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue.length() < 3) {
                if (!newValue.matches("\\d*")) {
                    itemLevelField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            } else {
                itemLevelField.setText(oldValue);
            }
        });
        itemQualityField.textProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue.length() < 3) {
                if (!newValue.matches("\\d*")) {
                    itemQualityField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            } else {
                itemQualityField.setText(oldValue);
            }
        });
    }

    @FXML
    void search(ActionEvent event) {
        setCursor(Cursor.WAIT);
        searchBtn.setDisable(true);
        clearTable();
        try {
            copyToClipboard();
            fetchItems();
        } finally {
            setCursor(Cursor.DEFAULT);
            searchBtn.setDisable(false);
        }
    }

    @FXML
    public void menuDragged(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuDraggedForGame(mouseEvent);
    }

    @FXML
    public void menuPressed(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuPressed(mouseEvent);
    }

    @FXML
    void closeBtn(ActionEvent event) {
        poeTradeManager.mainWindowWasVisible = false;
        viewFactory.getStage("MainWindow").hide();
    }

    private ScrollBar getVerticalScrollBar() {
        for (var node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                    return scrollBar;
                }
            }
        }
        return null;
    }

    @FXML
    void reloadData(ScrollEvent event) {
        ResultForQuery response = ResultForQuery.getInstance();
        if (response == null || response.getResult() == null) return;
        if( response.getResult().size() == table.getItems().size()) return;
        fetchItems();
    }

    private void setCursor(Cursor cursor) {
        if (stage != null && stage.getScene() != null) {
            stage.getScene().setCursor(cursor);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public VBox getVbox() {
        return mods;
    }

    public void clearTable() {
        table.getItems().clear();
    }

    public void addItemToTable(ItemDetails itemDetails) {
        table.getItems().add(itemDetails);
    }

    public Label getPriceDivine() {
        return priceDivine;
    }

    private void fetchItems() {
        if(WindowDetector.getGameWindow("Path of Exile 2") != null) {
            POE2 poe2 = new POE2(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods);
            poe2.fetchItems();
        } else if(WindowDetector.getGameWindow("Path of Exile") != null){
            POE poe = new POE(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods);
            poe.fetchItems();
        }
    }

    private void parseAndAddItem(JsonNode resultNode) {
        JsonNode priceNode = resultNode.path("listing").path("price");
        String price = priceNode.path("amount").asInt() + " " + priceNode.path("currency").asText();

        int level = resultNode.path("item").path("ilvl").asInt();
        addItemToTable(new ItemDetails(String.valueOf(level), price));
    }

    private void copyToClipboard() {
        try {
            String clipboardContent = ClipboardContent.getClipboardContent();

            if(WindowDetector.getGameWindow("Path of Exile 2") != null) {
                POE2 poe2 = new POE2(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods);
                Map<String, String> item = ParserData.parseItemData(clipboardContent);
                poe2.searchItems(item);
            } else if(WindowDetector.getGameWindow("Path of Exile") != null){
                Map<String, String> item = ParserData.parseItemData(clipboardContent);
                POE poe = new POE(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods);
                poe.searchItems(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setVisibleWindow(boolean isVisible){
        poeTradeManager.mainWindowWasVisible = isVisible;
    }


    public void addMods(VBox vbox) {
        if (!vbox.getChildren().isEmpty()) vbox.getChildren().clear();
        if(!WindowDetector.isPoEActive()) return;
        String data = ClipboardContent.getClipboardContent();
        Map<String, String> itemData = ParserData.parseItemData(data);
        itemLevelField.setText(ParserData.findValueForFilters(data, "Item Level: "));
        itemQualityField.setText(ParserData.findValueForFilters(data, "Quality: +"));

        if(itemData != null && (itemData.containsKey("Enchant"))) {
            for (String text : itemData.get("Enchant").split("\n")) {
                vbox.getChildren().add(cretaeHBox(text));
            }
        }

        if(itemData != null && (itemData.containsKey("Implicit"))) {
            for (String text : itemData.get("Implicit").split("\n")) {
                vbox.getChildren().add(cretaeHBox(text));
            }
        }

        if(itemData != null && (itemData.containsKey("Rune"))) {
            for (String text : itemData.get("Rune").split("\n")) {
                vbox.getChildren().add(cretaeHBox(text));
            }
        }

        if(itemData != null && (itemData.containsKey("Mods"))) {
            for (String text : itemData.get("Mods").split("\n")) {
                vbox.getChildren().add(cretaeHBox(text));
            }
        }
    }

    private HBox cretaeHBox(String text){
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        Label mod = new Label(ParserData.replaceNumberToHash(text));
        mod.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 11px;");
        TextField minField = new TextField();
        minField.textProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue.length() < 4) {
                if (!newValue.matches("\\d*")) {
                    minField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            } else {
                minField.setText(oldValue);
            }
        });
        minField.setPromptText("MIN");
        minField.setStyle("-fx-pref-width: 50px; -fx-min-width: 50px;");
        TextField maxField = new TextField();
        maxField.textProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue.length() < 4) {
                if (!newValue.matches("\\d*")) {
                    maxField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            } else {
                maxField.setText(oldValue);
            }
        });
        maxField.setPromptText("MAX");
        maxField.setStyle("-fx-pref-width: 50px; -fx-min-width: 50px;");
        List<String> minAndMax = ParserData.getNumberFromText(text);

        if(minAndMax.size() > 0){
            minField.setText(exactOrAllowMod(Integer.parseInt(minAndMax.get(0))));
        }

        Region spacer1 = new Region();
        Region spacer2 = new Region();

        HBox hBox = new HBox(10, checkBox, spacer1, mod, spacer2, minField, maxField);
        hBox.setStyle("-fx-padding: 5px;");
        hBox.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
        mod.setAlignment(Pos.BASELINE_CENTER);
        mod.setWrapText(true);
        return hBox;
    }

    private String exactOrAllowMod(int value){
       /* if(allowMod.isSelected()){
            return String.valueOf((int)(value * 0.9));
        } else {*/
            return String.valueOf(value);

    }

    @FXML
    void openPoeTrade(ActionEvent event) {
        String clipboardContent = ClipboardContent.getClipboardContent();
        Map<String, String> item = ParserData.parseItemData(clipboardContent);

        assert item != null;
        assert item.size() != 0;

         if(WindowDetector.getGameWindow("Path of Exile 2") != null) {
            String json = QuerySearch.create(mods, itemLevelField, itemQualityField, isCorrupted).createQueryForItem(item);

            String leagues = Settings.getInstance().get("leaguesPOE2") != null ? Settings.getInstance().get("leaguesPOE2").getValue() : "Standard";
            openWebPage("https://www.pathofexile.com/trade2/search/poe2/"+ leagues+"?q=" + URLEncoderE.encodeUrlFragment(json));
        } else if(WindowDetector.getGameWindow("Path of Exile") != null){
            String json = com.demo.poe.Service.poe.QuerySearch.create(mods, itemLevelField, itemQualityField, isCorrupted).createQuery(item);

            String leagues = Settings.getInstance().get("leaguesPOE") != null ? Settings.getInstance().get("leaguesPOE").getValue() : "Standard";
            openWebPage("https://www.pathofexile.com/trade/search/"+ leagues+"?q=" + URLEncoderE.encodeUrlFragment(json));
        }
    }

    private void openWebPage(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                URI uri = new URI(url);
                desktop.browse(uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Nie udało się otworzyć strony.");
        }
    }

}