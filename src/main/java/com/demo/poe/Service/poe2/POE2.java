package com.demo.poe.Service.poe2;

import com.demo.poe.Model.ItemDetails;
import com.demo.poe.Model.Json.Filters.FilterResponse;
import com.demo.poe.Model.Json.Filters.ItemOption;
import com.demo.poe.Model.Json.ResultForQuery;
import com.demo.poe.Model.Json.Stats.Entry;
import com.demo.poe.Model.Json.Stats.StaticData;
import com.demo.poe.Model.Mod;
import com.demo.poe.Model.Settings;
import com.demo.poe.Service.ClipboardContent;
import com.demo.poe.Service.POEApi;
import com.demo.poe.Service.ParserData;
import com.demo.poe.Service.ValidateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class POE2 implements POEApi {

    private static final String TRADE_API_BASE_URL = "https://www.pathofexile.com/api/trade2/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final int MAXRESULTRESPONS = 9;
    private TableView<ItemDetails> table;
    private Label resultNotFound;
    private TextField itemLevelField;
    private TextField itemQualityField;
    private CheckBox isCorrupted;
    private VBox mods;
    private String leagues = "Standard";

    public POE2(TableView<ItemDetails> table, Label resultNotFound, TextField itemLevelField, TextField itemQualityField, CheckBox isCorrupted, VBox mods) {
        this.table = table;
        this.resultNotFound = resultNotFound;
        this.itemLevelField = itemLevelField;
        this.itemQualityField = itemQualityField;
        this.isCorrupted = isCorrupted;
        this.mods = mods;
        leagues = Settings.getInstance().get("leaguesPOE2") != null ? Settings.getInstance().get("leaguesPOE2").getValue() : "Standard";
    }

    @Override
    public void fetchItems() {
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
                .thenApply(httpResponse -> ValidateResponse.getBodyWithLimit(httpResponse,resultNotFound))
                .thenAcceptAsync(this::processFetchResponse)
                .join();
    }

    @Override
    public void searchItems(Map<String, String> item) {
        String json = QuerySearch.create(mods, itemLevelField, itemQualityField, isCorrupted).createQuery(item);
        String url = TRADE_API_BASE_URL + "search/"+ (leagues.isEmpty() ? leagues : "Standard");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> ValidateResponse.getBodyWithLimit(httpResponse,resultNotFound))
                .thenAcceptAsync(ResultForQuery::loadDataFromRequest)
                .join();

        resultNotFound.setText("Matched " + ResultForQuery.getInstance().getTotal());
        resultNotFound.setStyle("-fx-font-size: 14px;");
    }

    private String generateItemsCode(ResultForQuery response) {
        int limit = Math.min(response.getResult().size(), MAXRESULTRESPONS);
        int startIndex = table.getItems().size() == 0 ? 0 : table.getItems().size() + 1;
        int endIndex = table.getItems().size() == 0
                ? limit
                : ((table.getItems().size() / limit) + 1) * limit;
        if (endIndex >= 18 || response.getResult().size() == table.getItems().size()) return "";
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

    public void addItemToTable(ItemDetails itemDetails) {
        table.getItems().add(itemDetails);
    }
}
