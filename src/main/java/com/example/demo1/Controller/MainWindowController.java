package com.example.demo1.Controller;

import com.example.demo1.Model.Json.Entry;
import com.example.demo1.Model.Json.StaticData;
import com.example.demo1.Service.ClipboardContent;
import com.example.demo1.Service.SettingsManager;
import com.example.demo1.Service.WindowDetector;
import com.example.demo1.HelloApplication;
import com.example.demo1.Model.Json.ItemDetails;
import com.example.demo1.Model.Json.ResultForQuery;
import com.example.demo1.Model.Mod;
import com.example.demo1.PoeTradeManager;
import com.example.demo1.Service.ParserData;
import com.example.demo1.View.ViewFactory;
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
import java.util.concurrent.CompletableFuture;
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
    private CheckBox allowMod;

    @FXML
    private CheckBox exactMod;

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

        allowMod.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                exactMod.setSelected(false);
            }
        });

        exactMod.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                allowMod.setSelected(false);
            }
        });
        ;

        iconApplication.setImage(new Image(Objects.requireNonNull(HelloApplication.class.getResourceAsStream(SettingsManager.getInstance().getSetting("icon32")))));
        titleApplicationLabel.setText(SettingsManager.getInstance().getSetting("title"));

        ScrollBar verticalScrollBar = getVerticalScrollBar();

        if (verticalScrollBar != null) {
            verticalScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0) {
                    fetchItems();
                }
            });
        }
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

            // Ograniczenie przesuwania do granic okna gry
            if (newX < gameWindowRect.left) {
                newX = gameWindowRect.left; // Lewa granica
            }
            if (newX + stage.getWidth() > gameWindowRect.right) {
                newX = gameWindowRect.right - stage.getWidth(); // Prawa granica
            }
            if (newY < gameWindowRect.top) {
                newY = gameWindowRect.top; // Górna granica
            }
            if (newY + stage.getHeight() > gameWindowRect.bottom) {
                newY = gameWindowRect.bottom - stage.getHeight(); // Dolna granica
            }

            // Ustaw nową pozycję okna
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
            this.gameWindowRect = gameWindowRect; // Zapisz granice okna gry
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
        if (response == null && response.getResult() != null) return;

        String itemsCode = generateItemsCode(response);
        if (itemsCode.isEmpty()) return;
        String url = TRADE_API_BASE_URL + "fetch/" + itemsCode + "?query=" + response.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAcceptAsync(this::processFetchResponse)
                .join();
    }

    private String generateItemsCode(ResultForQuery response) {
        int limit = Math.min(response.getResult().size(), MAXRESULTRESPONS);
        int startIndex = table.getItems().size() == 0 ? 0 : table.getItems().size() + 1;
        int endIndex = table.getItems().size() == 0
                ? limit
                : ((table.getItems().size() / limit) + 1) * limit;
        if (endIndex > 18) return "";
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
            if (item.size() == 0) return;
            String json = createQuery(item);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.pathofexile.com/api/trade2/search/Standard"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            CompletableFuture<HttpResponse<String>> futureResponse = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            CompletableFuture<Void> result = futureResponse
                    .thenApply(response -> {

                        String remaining = response.headers()
                                .firstValue("x-rate-limit-rules")
                                .orElse("Unknown");

                        System.out.println("Remaining rate limit: " + remaining);

                        String limitIP = response.headers()
                                .firstValue("x-rate-limit-" + remaining)
                                .orElse("Unknown");
                        System.out.println("limit per ip:" + limitIP);

                        String limitIPState = response.headers()
                                .firstValue("x-rate-limit-" + remaining + "-state")
                                .orElse("Unknown");
                        System.out.println("limit per ip:" + limitIPState);

                        return response.body(); // Przekazanie ciała dalej
                    })
                    .thenAcceptAsync(ResultForQuery::loadDataFromRequest)
                    .exceptionally(e -> {
                        System.err.println(e);
                        return null;
                    });
            result.join();
            resultNotFound.setText(String.valueOf(ResultForQuery.getInstance().getResult().size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createQuery(Map<String, String> item) {
        List<Mod> mods = createMods(Arrays.stream(item.get("Mods").split("\n")).toList());

        List<Mod> modsWithId = mods.stream()
                .flatMap(mod -> StaticData.getInstance().getResults().stream()
                        .flatMap(result -> findIdsByText(result.getEntries(), mod)))
                .collect(Collectors.toList());

        System.out.println(modsWithId);
        StringBuilder query = new StringBuilder();
        query.append("{\"query\":{\"status\":{\"option\":\"online\"},\"stats\":[{\"type\":\"and\",\"filters\":[");

        for (int i = 0; i < modsWithId.size(); i++) {
            query.append("{\"id\": \"").append(modsWithId.get(i).getId()).append("\",\"value\":");
            if (modsWithId.get(i).getValue() != null) {
                query.append("{\"min\":").append(modsWithId.get(i).getValue()).append("},\"disabled\": false}");
            } else if (modsWithId.get(i).getValueMin() != null) {
                query.append("{\"min\":").append(modsWithId.get(i).getValueMin()).append("},\"disabled\": false}");
            } else {
                query.append("{},\"disabled\": false}");
            }

            if (i < modsWithId.size() - 1) query.append(",");
        }

        query.append("]}]},\"sort\":{\"price\":\"asc\"}}");
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

    public boolean findModInVBox(String text) {
        for (var node : mods.getChildren()) {
            HBox hbox = (HBox) node;
            boolean checkBoxIsSelect = false;
            boolean correctMod = false;
            for (var element : hbox.getChildren()) {
                if (element instanceof CheckBox checkBox) {
                    checkBoxIsSelect = checkBox.isSelected();
                }
                if (element instanceof Label mod) {
                    if (mod.getText().equals(text)) correctMod = true;
                }

                if (checkBoxIsSelect && correctMod) return true;
            }
        }
        return false;
    }


    public void addMods(VBox vbox) {
        if (!vbox.getChildren().isEmpty()) vbox.getChildren().clear();
        Map<String, String> itemData = ParserData.parseItemData(ClipboardContent.getClipboardContent());
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
        minField.setPromptText("MIN");
        minField.setStyle("-fx-pref-width: 50px; -fx-min-width: 50px;");
        TextField maxField = new TextField();
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
        if(allowMod.isSelected()){
            return String.valueOf((int)(value * 0.9));
        } else {
            return String.valueOf(value);
        }
    }

}