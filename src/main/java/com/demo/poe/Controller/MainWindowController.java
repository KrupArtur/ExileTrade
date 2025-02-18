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
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.Cursor;
import javafx.scene.image.Image;

public class MainWindowController extends BaseController {
    private static final MenuDraggedAndPressed menuDraggedAndPressed = new MenuDraggedAndPressed();
    private static final Logger logger = Logger.getLogger(MainWindowController.class.getName());
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
        logger.info("Initializing MainWindowController");
        initializeTableColumns();
        initializeApplicationIcons();
        initializeScrollListener();
        initializeTextFields();
    }

    private void initializeTableColumns() {
        lvl.setCellValueFactory(new PropertyValueFactory<>("level"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));
        logger.info("Table columns initialized");
    }

    private void initializeApplicationIcons() throws IOException {
        iconApplication.setImage(loadImage(SettingsManager.getInstance().getSetting("icon32")));
        divineOrb.setImage(loadImage(getOrbImagePath("divine")));
        exileOrb.setImage(loadImage(getOrbImagePath("exile")));
        titleApplicationLabel.setText(SettingsManager.getInstance().getSetting("title"));
        logger.info("Application icons initialized");
    }

    private Image loadImage(String path) throws IOException {
        return new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(path)));
    }

    private String getOrbImagePath(String orbType) throws IOException {
        return WindowDetector.getGameWindow("Path of Exile") != null ?
                SettingsManager.getInstance().getSetting(orbType + "POE") : SettingsManager.getInstance().getSetting(orbType);
    }

    private void initializeScrollListener() {
        ScrollBar verticalScrollBar = getVerticalScrollBar();
        if (verticalScrollBar != null) {
            verticalScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0 && ResultForQuery.getInstance().getResult().size() > 9) {
                    fetchItems();
                }
            });
        }
        logger.info("Scroll listener initialized");
    }

    private void initializeTextFields() {
        setupTextField(itemLevelField);
        setupTextField(itemQualityField);
        logger.info("Text fields initialized");
    }

    private void setupTextField(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 3 && !newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                textField.setText(oldValue);
            }
        });
    }

    @FXML
    void search(ActionEvent event) {
        logger.info("Search button clicked");
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
        logger.info("Search action completed");
    }

    @FXML
    public void menuDragged(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuDraggedForGame(mouseEvent);
        logger.info("Menu dragged");
    }

    @FXML
    public void menuPressed(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuPressed(mouseEvent);
        logger.info("Menu pressed");
    }

    @FXML
    void closeBtn(ActionEvent event) {
        logger.info("Close button clicked");
        poeTradeManager.mainWindowWasVisible = false;
        viewFactory.getStage("MainWindow").hide();
    }

    private ScrollBar getVerticalScrollBar() {
        for (var node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
    }

    @FXML
    void reloadData(ScrollEvent event) {
        logger.info("Reload data triggered by scroll event");
        if (isReloadRequired()) {
            fetchItems();
        }
    }

    private boolean isReloadRequired() {
        ResultForQuery response = ResultForQuery.getInstance();
        return response != null && response.getResult() != null && response.getResult().size() > table.getItems().size();
    }

    private void setCursor(Cursor cursor) {
        if (stage != null && stage.getScene() != null) {
            stage.getScene().setCursor(cursor);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        logger.info("Stage set");
    }

    public VBox getVbox() {
        return mods;
    }

    public void clearTable() {
        table.getItems().clear();
        logger.info("Table cleared");
    }

    public void addItemToTable(ItemDetails itemDetails) {
        table.getItems().add(itemDetails);
        logger.info("Item added to table: " + itemDetails);
    }

    public Label getPriceDivine() {
        return priceDivine;
    }

    private void fetchItems() {
        logger.info("Fetching items");
        if (WindowDetector.getGameWindow("Path of Exile 2") != null) {
            new POE2(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods).fetchItems();
        } else if (WindowDetector.getGameWindow("Path of Exile") != null) {
            new POE(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods).fetchItems();
        }
        logger.info("Items fetched");
    }

    private void parseAndAddItem(JsonNode resultNode) {
        String price = resultNode.path("listing").path("price").path("amount").asInt() + " " + resultNode.path("listing").path("price").path("currency").asText();
        int level = resultNode.path("item").path("ilvl").asInt();
        addItemToTable(new ItemDetails(String.valueOf(level), price));
        logger.info("Item parsed and added to table: Level " + level + ", Price " + price);
    }

    private void copyToClipboard() {
        try {
            String clipboardContent = ClipboardContent.getClipboardContent();
            if (WindowDetector.getGameWindow("Path of Exile 2") != null) {
                searchItemsWithPoe2(clipboardContent);
            } else if (WindowDetector.getGameWindow("Path of Exile") != null) {
                searchItemsWithPoe(clipboardContent);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error copying to clipboard", e);
        }
    }

    private void searchItemsWithPoe2(String clipboardContent) {
        POE2 poe2 = new POE2(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods);
        Map<String, String> item = ParserData.parseItemData(clipboardContent);
        poe2.searchItems(item);
        logger.info("Items searched with POE2");
    }

    private void searchItemsWithPoe(String clipboardContent) {
        POE poe = new POE(table, resultNotFound, itemLevelField, itemQualityField, isCorrupted, mods);
        Map<String, String> item = ParserData.parseItemData(clipboardContent);
        poe.searchItems(item);
        logger.info("Items searched with POE");
    }

    public void setVisibleWindow(boolean isVisible) {
        poeTradeManager.mainWindowWasVisible = isVisible;
        logger.info("Window visibility set to " + isVisible);
    }

    public void addMods(VBox vbox) {
        if (!vbox.getChildren().isEmpty()) vbox.getChildren().clear();
        if (!WindowDetector.isPoEActive()) return;
        String data = ClipboardContent.getClipboardContent();
        Map<String, String> itemData = ParserData.parseItemData(data);
        itemLevelField.setText(ParserData.findValueForFilters(data, "Item Level: "));
        itemQualityField.setText(ParserData.findValueForFilters(data, "Quality: +"));
        addModItemsToVBox(vbox, itemData);
        logger.info("Mods added to VBox");
    }

    private void addModItemsToVBox(VBox vbox, Map<String, String> itemData) {
        if (itemData != null && itemData.containsKey("Enchant")) {
            addModItems(vbox, itemData.get("Enchant"));
        }
        if (itemData != null && itemData.containsKey("Implicit")) {
            addModItems(vbox, itemData.get("Implicit"));
        }
        if (itemData != null && itemData.containsKey("Rune")) {
            addModItems(vbox, itemData.get("Rune"));
        }
        if (itemData != null && itemData.containsKey("Mods")) {
            addModItems(vbox, itemData.get("Mods"));
        }
    }

    private void addModItems(VBox vbox, String modText) {
        for (String text : modText.split("\n")) {
            vbox.getChildren().add(createModHBox(text));
        }
    }



    private HBox createModHBox(String text) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        Label mod = new Label(ParserData.replaceNumberToHash(text));
        mod.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 11px;");
        TextField minField = createModTextField("MIN");
        TextField maxField = createModTextField("MAX");
        setModTextFieldValues(text, minField, maxField);
        HBox hBox = new HBox(10, checkBox, new Region(), mod, new Region(), minField, maxField);
        hBox.setStyle("-fx-padding: 5px;");
        hBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hBox.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(hBox.getChildren().get(3), javafx.scene.layout.Priority.ALWAYS);
        mod.setAlignment(Pos.BASELINE_CENTER);
        mod.setWrapText(true);
        logger.info("Mod HBox created: " + text);
        return hBox;
    }

    private TextField createModTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle("-fx-pref-width: 50px; -fx-min-width: 50px;");
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 4 && !newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                textField.setText(oldValue);
            }
        });
        return textField;
    }

    private void setModTextFieldValues(String text, TextField minField, TextField maxField) {
        List<String> minAndMax = ParserData.getNumberFromText(text);
        if (!minAndMax.isEmpty()) {
            minField.setText(exactOrAllowMod(Integer.parseInt(minAndMax.get(0))));
        }
    }

    private String exactOrAllowMod(int value) {
        return String.valueOf(value);
    }

    @FXML
    void openPoeTrade(ActionEvent event) {
        logger.info("Open POE Trade button clicked");
        String clipboardContent = ClipboardContent.getClipboardContent();
        Map<String, String> item = ParserData.parseItemData(clipboardContent);

        assert item != null;
        assert item.size() != 0;

        if (WindowDetector.getGameWindow("Path of Exile 2") != null) {
            openPoeTradeForPoe2(item);
        } else if (WindowDetector.getGameWindow("Path of Exile") != null) {
            openPoeTradeForPoe(item);
        }
    }

    private void openPoeTradeForPoe2(Map<String, String> item) {
        String json = QuerySearch.create(mods, itemLevelField, itemQualityField, isCorrupted).createQueryForItem(item);
        String leagues = Optional.ofNullable(Settings.getInstance().get("leaguesPOE2")).map(Settings::getValue).orElse("Standard");
        openWebPage("https://www.pathofexile.com/trade2/search/poe2/" + leagues + "?q=" + URLEncoderE.encodeUrlFragment(json));
        logger.info("POE Trade opened for POE2");
    }

    private void openPoeTradeForPoe(Map<String, String> item) {
        String json = com.demo.poe.Service.poe.QuerySearch.create(mods, itemLevelField, itemQualityField, isCorrupted).createQuery(item);
        String leagues = Optional.ofNullable(Settings.getInstance().get("leaguesPOE")).map(Settings::getValue).orElse("Standard");
        openWebPage("https://www.pathofexile.com/trade/search/" + leagues + "?q=" + URLEncoderE.encodeUrlFragment(json));
        logger.info("POE Trade opened for POE");
    }

    private void openWebPage(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                URI uri = new URI(url);
                desktop.browse(uri);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to open web page", e);
        }
    }
}