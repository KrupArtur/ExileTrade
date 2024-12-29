package com.demo.poe.Controller;

import com.demo.poe.HelloApplication;
import com.demo.poe.Model.Json.Filters.FilterResponse;
import com.demo.poe.Model.Json.Filters.ItemOption;
import com.demo.poe.Model.Json.Stats.Entry;
import com.demo.poe.Model.ItemDetails;
import com.demo.poe.Model.Json.ResultForQuery;
import com.demo.poe.Model.Json.Stats.StaticData;
import com.demo.poe.Model.Mod;
import com.demo.poe.PoeTradeManager;
import com.demo.poe.Service.*;
import com.demo.poe.View.ViewFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.platform.win32.WinDef;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.Cursor;
import javafx.scene.image.Image;

public class MainWindowController extends BaseController {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TRADE_API_BASE_URL = "https://www.pathofexile.com/api/trade2/";
    private static final int MAXRESULTRESPONS = 9;
    private WinDef.RECT gameWindowRect;
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

    private double xOffset = 0;
    private double yOffset = 0;

    public MainWindowController(PoeTradeManager poeTradeManager, ViewFactory viewFactory, String fxmlName) {
        super(poeTradeManager, viewFactory, fxmlName);
    }


    @FXML
    public void initialize() {
        lvl.setCellValueFactory(new PropertyValueFactory<>("level"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));

        iconApplication.setImage(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(SettingsManager.getInstance().getSetting("icon32")))));
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

    @FXML
    public void menuPressed(MouseEvent mouseEvent) {
        WinDef.RECT gameWindowRect = WindowDetector.getGameWindow("Path of Exile 2");
        if (gameWindowRect != null) {
            xOffset = mouseEvent.getSceneX();
            yOffset = mouseEvent.getSceneY();
            this.gameWindowRect = gameWindowRect;
        }
    }

    @FXML
    void closeBtn(ActionEvent event) {
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

    private void fetchItems() {
        ResultForQuery response = ResultForQuery.getInstance();
        if (response == null || response.getResult() == null) return;

        String itemsCode = generateItemsCode(response);
        if (itemsCode.isEmpty()) return;

        String url = TRADE_API_BASE_URL + "fetch/" + itemsCode + "?query=" + response.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("User-Agent", "ExileTrader/0.1")
                .GET()
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::getBodyWithLimit)
                .thenAcceptAsync(this::processFetchResponse)
                .join();
    }

    private String generateItemsCode(ResultForQuery response) {
        int limit = Math.min(response.getResult().size(), MAXRESULTRESPONS);
        int startIndex = table.getItems().size() == 0 ? 0 : table.getItems().size() + 1;
        int endIndex = table.getItems().size() == 0
                ? limit
                : ((table.getItems().size() / limit) + 1) * limit;
        if (endIndex > 18 || response.getResult().size() == table.getItems().size()) return "";
        return String.join(",", response.getResult().subList(startIndex, endIndex));
    }

    private void processFetchResponse(String responseBody) {
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(responseBody);
            if (rootNode == null || !rootNode.has("result")) return;

            rootNode.path("result").forEach(this::parseAndAddItem);

            ScrollBar verticalScrollBar = getVerticalScrollBar();
            if (verticalScrollBar != null) {

                verticalScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.doubleValue() == 1.0) {
                        System.out.println("Przewinięto na sam dół!");
                        fetchItems();
                    }
                });
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
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
            Map<String, String> item = ParserData.parseItemData(clipboardContent);

            assert item != null;
            assert item.size() != 0;

            String json = createQuery(item);

            String url = TRADE_API_BASE_URL + "search/Standard";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::getBodyWithLimit)
                    .thenAcceptAsync(ResultForQuery::loadDataFromRequest)
                    .join();

            resultNotFound.setText("Matched " + ResultForQuery.getInstance().getTotal());
            resultNotFound.setStyle("-fx-font-size: 14px;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBodyWithLimit(HttpResponse response) {
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

    public String createQuery(Map<String, String> item) {
        List<Mod> mods = createMods(Arrays.stream(item.get("Mods").split("\n")).toList());
        List<Mod> combined = Stream.concat(mods.stream(), getModFromGUI().stream())
                .filter(mod -> mod.getName() != null && !mod.getName().isEmpty())
                .collect(Collectors.toMap(
                        Mod::getName,
                        Function.identity(),
                        (mod1, mod2) -> mod1
                ))
                .values()
                .stream()
                .toList();

        List<Mod> modsWithId = combined.stream()
                .flatMap(mod -> StaticData.getInstance().getResults().stream()
                        .flatMap(result -> findIdsByText(result.getEntries(), mod))).toList();

        StringBuilder query = new StringBuilder();
        query.append("{\"query\":{\"status\":{\"option\":\"online\"},\"stats\":[{\"type\":\"and\",\"filters\":[");

        for (int i = 0; i < modsWithId.size(); i++) {
            query.append("{\"id\": \"").append(modsWithId.get(i).getId()).append("\",\"value\":");
            if (modsWithId.get(i).getValue() != null) {
                query.append("{\"min\":").append(modsWithId.get(i).getValue()).append("},\"disabled\": false}");
            } else if (modsWithId.get(i).getValueMin() != null && !modsWithId.get(i).getValueMin().isEmpty()) {
                query.append("{\"min\":").append(modsWithId.get(i).getValueMin()).append("},\"disabled\": false}");
            } else {
                query.append("{},\"disabled\": false}");
            }

            if (i < modsWithId.size() - 1) query.append(",");
        }
        String itemClass = ParserData.findNameForFilters(ClipboardContent.getClipboardContent(), "Item Class: ");
        boolean isItemLevelOrQualityOrCorrupted = false;
        if((itemLevelField.getText() != null && !itemLevelField.getText().isEmpty()) ||
                (itemQualityField.getText() != null && !itemQualityField.getText().isEmpty()) ||
                !itemClass.isEmpty()) {
            isItemLevelOrQualityOrCorrupted = true;
            query.append("]}], \"filters\": { \"type_filters\": { \"filters\": {");
            if(!itemLevelField.getText().isEmpty()){
                query.append("\"ilvl\": { \"min\": ") .append(itemLevelField.getText()).append("}");
            }

            if (!itemQualityField.getText().isEmpty()){
                if(query.toString().contains("\"ilvl\"")) query.append(",");
                query.append("\"quality\": { \"min\": ").append(itemQualityField.getText()).append("}");
            }

            if(!itemClass.isEmpty()){
                if(query.toString().contains("\"ilvl\"") || query.toString().contains("\"quality\"") ) query.append(",");
                query.append("\"category\":{\"option\":\"")
                        .append(FilterResponse.getInstance().getFilters().stream()
                                .flatMap(filterType -> filterType.getFilters().stream())
                                .filter(filters -> filters.getText().equals("Item Category"))
                                .flatMap(filter -> filter.getOption().getOptions().stream())
                                .filter(filterOption -> itemClass.contains(filterOption.getText()))
                                .map(ItemOption::getId).findFirst().orElse("")).append("\"}");

            }

            if(isCorrupted.isSelected())
                query.append("}},");
            else
                query.append("}}}},");

        }

        if(isCorrupted.isSelected()){
            isItemLevelOrQualityOrCorrupted = true;
            query.append("\"misc_filters\":{\"disabled\":false,\"filters\":{\"corrupted\":{\"option\":\"")
                    .append(isCorrupted.isSelected()).append("\"");
            query.append("}}}}},");
        }
        if(!isItemLevelOrQualityOrCorrupted){
            query.append("]}]},");
        }

        query.append("\"sort\":{\"price\":\"asc\"}}");

        return query.toString();
    }

    public List<Mod> createMods(List<String> mods) {
        return mods.stream().map(mod -> {
            List<String> values = ParserData.getNumberFromText(mod);
            if (values.size() > 1) {
                return new Mod(ParserData.replaceNumberToHash(mod), values.get(0), values.get(1));
            } else {
                return new Mod(ParserData.replaceNumberToHash(mod), values.get(0));
            }
        }).collect(Collectors.toList());
    }

    public Stream<Mod> findIdsByText(List<Entry> entries, Mod mod) {
        return entries.stream()
                .filter(entry -> {
                    if (exclusionsItem(entry) && findModInVBox(mod.getName())) {
                        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                        Matcher matcher = pattern.matcher(entry.getText());
                        String text = entry.getText();
                        while (matcher.find()) {
                            String element = matcher.group(1);
                            if (element.contains("|")) {
                                List<String> parts = Arrays.stream(element.split("\\|")).toList();
                                String prevent = "";
                                for (String part : parts) {
                                    text = text.replace("[" + element + "]", part);
                                    if (text.equals(mod.getName()) && exclusionsItem(entry)) {
                                        return true;
                                    }
                                    if (text.replace(prevent, part).equals(mod.getName()) && exclusionsItem(entry)) {
                                        return true;
                                    }
                                    prevent = part;
                                }
                            } else {
                                return entry.getText().equals(mod.getName());
                            }
                        }
                        return entry.getText().equals(mod.getName());
                    }
                    return false;
                })
                .map(entry -> {
                    mod.setId(entry.getId());
                    return mod;
                });
    }

    public boolean exclusionsItem(Entry entry) {
        return !entry.getId().contains("enchant") &&
                !entry.getId().contains("implicit") &&
                !entry.getId().contains("sanctum") &&
                !entry.getId().contains("rune") &&
                !entry.getId().equals("explicit.stat_3489782002");
    }
    public List<Mod> getModFromGUI(){
        List<Mod> modArrayList = new ArrayList<>();
        for (var node : mods.getChildren()) {
            if (node instanceof HBox hbox) {
                Mod m = new Mod();
                for (var element : hbox.getChildren()) {
                    if (element instanceof ComboBox mod) {
                        m.setName(mod.getValue().toString());
                    }
                    if(element instanceof TextField textField){
                        if(textField.getPromptText().equals("MIN")){
                            m.setValueMin(textField.getText());
                        } else if (textField.getPromptText().equals("MAX")){
                            m.setValueMax(textField.getText());
                        }
                    }
                }
                modArrayList.add(m);
            }
        }
        return modArrayList;
    }

    public boolean findModInVBox(String text) {
        for (var node : mods.getChildren()) {
            if (node instanceof HBox hbox) {
                boolean checkBoxIsSelect = false;
                boolean correctMod = false;
                for (var element : hbox.getChildren()) {
                    if (element instanceof CheckBox checkBox) {
                        checkBoxIsSelect = checkBox.isSelected();
                    }
                    if (element instanceof Label mod) {
                        if (mod.getText().equals(text)) correctMod = true;
                    }
                    if (element instanceof ComboBox comboBox) {
                        if (comboBox.getValue().equals(text)) correctMod = true;
                    }

                    if (checkBoxIsSelect && correctMod) return true;
                }
            }
        }
        return false;
    }


    public void addMods(VBox vbox) {
        if (!vbox.getChildren().isEmpty()) vbox.getChildren().clear();
        String data = ClipboardContent.getClipboardContent();
        Map<String, String> itemData = ParserData.parseItemData(data);
        itemLevelField.setText(ParserData.findValueForFilters(data, "Item Level: "));
        itemQualityField.setText(ParserData.findValueForFilters(data, "Quality: +"));
        if(itemData != null) {
            for (String text : itemData.get("Mods").split("\n")) {
                vbox.getChildren().add(cretaeHBox(text));
            }
            return;
        }
        for(int i = 0 ; i < 3; i++){
            Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            assert robot != null;
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);
            robot.delay(10);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        }
        itemData = ParserData.parseItemData(ClipboardContent.getClipboardContent());
        if(itemData != null) {
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

        String json = createQuery(item);
        openWebPage("https://www.pathofexile.com/trade2/search/poe2/Standard?q=" + URLEncoderE.encodeUrlFragment(json));
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