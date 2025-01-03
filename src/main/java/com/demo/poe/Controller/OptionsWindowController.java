package com.demo.poe.Controller;

import com.demo.poe.Events.MenuDraggedAndPressed;
import com.demo.poe.Model.Json.Filters.FilterResponse;
import com.demo.poe.Model.Json.Settings.Leagues.Leagues;
import com.demo.poe.Model.Settings;
import com.demo.poe.PoeTradeManager;
import com.demo.poe.Service.TempFile;
import com.demo.poe.View.ViewFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OptionsWindowController extends BaseController {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final MenuDraggedAndPressed menuDraggedAndPressed = new MenuDraggedAndPressed();
    @FXML
    private ComboBox<String> leaguesPOE;

    @FXML
    private ComboBox<String> leaguesPOE2;

    @FXML
    private CheckBox exactValuePoE;

    @FXML
    private CheckBox exactValuePoE2;

    @FXML
    private TextField fillStatAroundPoE;

    @FXML
    private TextField fillStatAroundPoE2;

    @FXML
    private TabPane tabPane;

    public OptionsWindowController(PoeTradeManager poeTradeManager, ViewFactory viewFactory, String fxmlName) {
        super(poeTradeManager, viewFactory, fxmlName);
    }

    @FXML
    public void initialize(){
        List<String> leaguesPOE2List = new ArrayList<>();
        leaguesPOE2List.add("Standard");
        leaguesPOE2List.add("Hardcore");
        leaguesPOE2.setItems(FXCollections.observableArrayList(leaguesPOE2List));
        getLeaguesToPoe();

        loadData();
    }

    private void loadData() {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(TempFile.loadSettings());

            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String value = rootNode.get(fieldName).asText();
                Settings.getInstance().put(fieldName, new Settings(fieldName, value));
            }

            for (Tab tab: tabPane.getTabs()) {
                if(tab.getContent() instanceof VBox vbox) {
                    for (var children : vbox.getChildren()) {
                        if (children instanceof HBox vboxWithHbox) {
                            for (var child : vboxWithHbox.getChildren()) {
                                if (child instanceof VBox mainVBox) {
                                    for (var node : mainVBox.getChildren()) {
                                        if (node instanceof ComboBox leagues) {
                                            if(Settings.getInstance().get(leagues.getId()) != null)
                                                leagues.setValue(Settings.getInstance().get(leagues.getId()).getValue());
                                        } else if (node instanceof TextField textField) {
                                            textField.setText(String.valueOf(Settings.getInstance().get(textField.getId()).getValue()));
                                        } else if (node instanceof CheckBox checkBox) {
                                            checkBox.setSelected(Boolean.parseBoolean(Settings.getInstance().get(checkBox.getId()).getValue()));
                                        } else if (node instanceof HBox mainHBox) {
                                            for (var mainHBoxChildren : mainHBox.getChildren()) {
                                                if (mainHBoxChildren instanceof ComboBox leagues) {
                                                    if(Settings.getInstance().get(leagues.getId()) != null)
                                                        leagues.setValue(Settings.getInstance().get(leagues.getId()).getValue());
                                                } else if (mainHBoxChildren instanceof TextField textField) {
                                                    if(Settings.getInstance().get(textField.getId()) != null)
                                                        textField.setText(String.valueOf(Settings.getInstance().get(textField.getId()).getValue()));
                                                } else if (mainHBoxChildren instanceof CheckBox checkBox) {
                                                    if(Settings.getInstance().get(checkBox.getId()) != null)
                                                        checkBox.setSelected(Boolean.parseBoolean(Settings.getInstance().get(checkBox.getId()).getValue()));
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    public void menuDragged(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuDragged(mouseEvent);
    }

    public void menuPressed(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuPressed(mouseEvent);
    }

    public void closeBtn(ActionEvent actionEvent) {
        viewFactory.getStage("OptionsWindow").close();
        viewFactory.removeStage("OptionsWindow");
        viewFactory.getStage("MainWindow").show();
    }

    @FXML
    void saveAction(ActionEvent event) {
        List<Settings> settingsList = new ArrayList<>();
        settingsList.add(new Settings("leaguesPOE2",leaguesPOE2.getValue()));
        settingsList.add(new Settings("leaguesPOE",leaguesPOE.getValue()));
        settingsList.add(new Settings("fillStatAroundPoE",fillStatAroundPoE.getText()));
        settingsList.add(new Settings("fillStatAroundPoE2",fillStatAroundPoE2.getText()));
        settingsList.add(new Settings("exactValuePoE",exactValuePoE.isSelected() + ""));
        settingsList.add(new Settings("exactValuePoE2",exactValuePoE2.isSelected() + ""));


        TempFile.saveConfig(settingsList);
    }

    @FXML
    void cancelAction(ActionEvent event) {
        viewFactory.getStage("OptionsWindow").close();
        viewFactory.removeStage("OptionsWindow");
    }

    private void getLeaguesToPoe(){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.pathofexile.com/api/leagues"))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "ExileTrader/0.1")
                    .GET()
                    .build();
            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAcceptAsync(this::accept)
                    .join();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void accept(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Leagues> leaguesList = objectMapper.readValue(body, new TypeReference<List<Leagues>>() {});
            List<String> z = leaguesList.stream().map(Leagues::getId).toList();
            leaguesPOE.setItems(FXCollections.observableArrayList(z));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
