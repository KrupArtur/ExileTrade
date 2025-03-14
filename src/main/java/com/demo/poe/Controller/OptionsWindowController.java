package com.demo.poe.Controller;

import com.demo.poe.Service.Events.MenuDraggedAndPressed;
import com.demo.poe.Model.Json.Leagues.Leagues;
import com.demo.poe.Model.Settings;
import com.demo.poe.PoeTradeManager;
import com.demo.poe.Service.TempFile;
import com.demo.poe.View.ViewFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OptionsWindowController extends BaseController {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final MenuDraggedAndPressed menuDraggedAndPressed = new MenuDraggedAndPressed();
    private static final Logger logger = Logger.getLogger(OptionsWindowController.class.getName());
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
    public void initialize() {
        logger.info("Initializing OptionsWindowController");
        initializeLeagues();
        getLeaguesToPoe();
        loadData();
    }

    private void initializeLeagues() {
        List<String> leaguesPOE2List = List.of("Standard", "Hardcore");
        leaguesPOE2.setItems(FXCollections.observableArrayList(leaguesPOE2List));
        logger.info("Leagues for POE2 initialized");
    }

    private void loadData() {
        try {
            String settings = TempFile.loadSettings();
            if (settings == null || settings.isEmpty()) {
                logger.warning("Settings are empty or null");
                return;
            }

            JsonNode rootNode = new ObjectMapper().readTree(settings);
            Iterator<String> fieldNames = rootNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String value = rootNode.get(fieldName).asText();
                Settings.getInstance().put(fieldName, new Settings(fieldName, value));
            }

            for (Tab tab : tabPane.getTabs()) {
                if (tab.getContent() instanceof VBox vbox) {
                    loadSettingsFromVBox(vbox);
                }
            }
            logger.info("Settings loaded successfully");

        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error processing settings JSON", e);
        }
    }

    private void loadSettingsFromVBox(VBox vbox) {
        for (var children : vbox.getChildren()) {
            if (children instanceof HBox vboxWithHbox) {
                for (var child : vboxWithHbox.getChildren()) {
                    if (child instanceof VBox mainVBox) {
                        loadSettingsFromMainVBox(mainVBox);
                    }
                }
            }
        }
    }

    private void loadSettingsFromMainVBox(VBox mainVBox) {
        for (var node : mainVBox.getChildren()) {
            if (node instanceof ComboBox leagues) {
                setComboBoxValue(leagues);
            } else if (node instanceof TextField textField) {
                setTextFieldValue(textField);
            } else if (node instanceof CheckBox checkBox) {
                setCheckBoxValue(checkBox);
            } else if (node instanceof HBox mainHBox) {
                loadSettingsFromMainHBox(mainHBox);
            }
        }
    }

    private void loadSettingsFromMainHBox(HBox mainHBox) {
        for (var mainHBoxChildren : mainHBox.getChildren()) {
            if (mainHBoxChildren instanceof ComboBox leagues) {
                setComboBoxValue(leagues);
            } else if (mainHBoxChildren instanceof TextField textField) {
                setTextFieldValue(textField);
            } else if (mainHBoxChildren instanceof CheckBox checkBox) {
                setCheckBoxValue(checkBox);
            }
        }
    }

    private void setComboBoxValue(ComboBox leagues) {
        if (Settings.getInstance().get(leagues.getId()) != null)
            leagues.setValue(Settings.getInstance().get(leagues.getId()).getValue());
    }

    private void setTextFieldValue(TextField textField) {
        if (Settings.getInstance().get(textField.getId()) != null)
            textField.setText(String.valueOf(Settings.getInstance().get(textField.getId()).getValue()));
    }

    private void setCheckBoxValue(CheckBox checkBox) {
        if (Settings.getInstance().get(checkBox.getId()) != null)
            checkBox.setSelected(Boolean.parseBoolean(Settings.getInstance().get(checkBox.getId()).getValue()));
    }



    public void menuDragged(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuDraggedForGame(mouseEvent);
        logger.info("Menu dragged");
    }

    public void menuPressed(MouseEvent mouseEvent) {
        menuDraggedAndPressed.menuPressed(mouseEvent);
        logger.info("Menu pressed");
    }

    public void closeBtn(ActionEvent actionEvent) {
        viewFactory.getStage("OptionsWindow").close();
        viewFactory.removeStage("OptionsWindow");
        if (poeTradeManager.mainWindowWasVisible)
            viewFactory.getStage("MainWindow").show();
        logger.info("Options window closed");
    }

    @FXML
    void saveAction(ActionEvent event) {
        List<Settings> settingsList = new ArrayList<>();
        settingsList.add(new Settings("leaguesPOE2", leaguesPOE2.getValue()));
        settingsList.add(new Settings("leaguesPOE", leaguesPOE.getValue()));
        settingsList.add(new Settings("fillStatAroundPoE", fillStatAroundPoE.getText()));
        settingsList.add(new Settings("fillStatAroundPoE2", fillStatAroundPoE2.getText()));
        settingsList.add(new Settings("exactValuePoE", exactValuePoE.isSelected() + ""));
        settingsList.add(new Settings("exactValuePoE2", exactValuePoE2.isSelected() + ""));

        TempFile.saveConfig(settingsList);
        logger.info("Settings saved");
    }

    @FXML
    void cancelAction(ActionEvent event) {
        viewFactory.getStage("OptionsWindow").close();
        viewFactory.removeStage("OptionsWindow");
        logger.info("Options window cancel action triggered");
    }

    private void getLeaguesToPoe() {
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
            logger.info("Leagues fetched from API");
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Error fetching leagues from API", e);
        }
    }

    private void accept(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Leagues> leaguesList = objectMapper.readValue(body, new TypeReference<List<Leagues>>() {});
            List<String> leagues = leaguesList.stream().map(Leagues::getId).toList();
            leaguesPOE.setItems(FXCollections.observableArrayList(leagues));
            logger.info("Leagues set to ComboBox");
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error processing leagues JSON", e);
        }
    }
}
