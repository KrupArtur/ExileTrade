package com.demo.poe.Service.Events;

import com.demo.poe.Model.POE2.Json.Stats.StaticData;
import com.demo.poe.Service.ClipboardContent;
import com.demo.poe.Service.WindowDetector;
import com.demo.poe.Controller.MainWindowController;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeyEventHandler implements NativeKeyListener {

    private Stage primaryStage;
    private MainWindowController controller;
    private boolean isWindowVisible = false;
    private String lastClipbordContent = "";

    public KeyEventHandler(Stage primaryStage, MainWindowController controller) {
        this.primaryStage = primaryStage;
        this.controller = controller;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if (WindowDetector.isPoEActive()) {
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_C && isCtrlPressed(nativeKeyEvent)) {
                Platform.runLater(this::toggleWindowVisibility);
            } else if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_D && isCtrlPressed(nativeKeyEvent)) {
                try {
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_C);
                    robot.delay(100);
                    robot.keyRelease(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                } catch (AWTException e) {
                    e.printStackTrace();
                }

            }
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F5) {
                sendHideoutCommand();
            }
        }
    }
    public static void bringPoE2ToFront() {
        String windowName = "Path of Exile 2";  // Nazwa okna gry

        User32 user32 = User32.INSTANCE;
        WinDef.HWND hwnd = findWindowByName(windowName);
        if (hwnd != null) {
            user32.SetForegroundWindow(hwnd);  // Przestawia okno na pierwszy plan
            user32.SetFocus(hwnd);
            System.out.println(user32.GetForegroundWindow());
        } else {
            System.out.println("Nie znaleziono okna: " + windowName);
        }
    }

    private static WinDef.HWND findWindowByName(String windowName) {
        User32 user32 = User32.INSTANCE;
        WinDef.HWND hwnd = user32.FindWindow(null, windowName);  // Znajduje okno po nazwie
        return hwnd;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if(WindowDetector.isPoEActive()) {
            if (!isWindowVisible && nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_C && isCtrlPressed(nativeKeyEvent)) {
                Platform.runLater(this::toggleWindowVisibility);
            } else if (!isWindowVisible && nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_D && isCtrlPressed(nativeKeyEvent)) {
                try {
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_CONTROL);
                    robot.keyPress(KeyEvent.VK_C);
                    robot.delay(10);
                    robot.keyRelease(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    private boolean isCtrlPressed(NativeKeyEvent nativeKeyEvent) {
        return (nativeKeyEvent.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
    }
    private static final int RETRY_LIMIT = 3; // Liczba prób
    private static final int RETRY_DELAY = 500; // Czas między próbami w ms

    private void toggleWindowVisibility() {
        if(!WindowDetector.isPoEActive()) return;
        String newClipbordContent = ClipboardContent.getClipboardContent();

        if(lastClipbordContent.equals(newClipbordContent)){
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    String initialContent = ClipboardContent.getClipboardContent();
                    int attempts = 0;

                    while (attempts < RETRY_LIMIT) {
                        try {
                            Thread.sleep(RETRY_DELAY);
                            String currentContent = ClipboardContent.getClipboardContent();

                            if (currentContent != null && !currentContent.equals(initialContent)) {
                                System.out.println("Clipboard updated: ");
                                break;
                            }

                            Robot robot = new Robot();
                            robot.keyPress(KeyEvent.VK_CONTROL);
                            robot.keyPress(KeyEvent.VK_C);
                            robot.delay(10);
                            robot.keyRelease(KeyEvent.VK_C);
                            robot.keyRelease(KeyEvent.VK_CONTROL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                        attempts++;
                    }

                    if (attempts == RETRY_LIMIT) {
                        System.out.println("Failed to retrieve clipboard content after retries.");
                    }

                    return null;
                }
            };
            new Thread(task).start();
        }

        controller.getVbox().getChildren().clear();
        controller.addMods(controller.getVbox());
        HBox hBox = new HBox();
        hBox.getChildren().add(createButtonToAddMods());
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5,5,5,5));
        controller.getVbox().getChildren().add(hBox);

        Button button = findButtonAddMods(controller.getVbox());

        if(sizeChildHbox(controller.getVbox()) > 8 && button != null){
            button.setDisable(true);
        }
        controller.clearTable();

        if (isWindowVisible && newClipbordContent.equals(lastClipbordContent)) {
            controller.setVisibleWindow(false);
            Platform.runLater(primaryStage::hide);
            isWindowVisible = false;
            lastClipbordContent = "";
        } else {
            controller.setVisibleWindow(true);
            lastClipbordContent = newClipbordContent;
            showWindowAtCursor();
            isWindowVisible = true;
        }
    }

    public int sizeChildHbox(VBox vBox){
        int i = 0;
        for (var node:vBox.getChildren()) {
            if(node instanceof HBox hBox){
                i++;
            }
        }
        return i;
    }

    public Button findButtonAddMods(VBox vBox){
        for (var node:vBox.getChildren()) {
            if(node instanceof HBox hBox){
                for (var children :hBox.getChildren()) {
                    if(children instanceof Button button)
                        return button;
                }
            }
        }
        return null;
    }

    public Button createButtonToAddMods(){
        Button buttton = new Button("Add Mods");
        buttton.setOnAction(e -> {
            ComboBox<String> combobox = new ComboBox<>();
            List<String> texts = StaticData.getInstance().getResults().get(0).getEntries().stream()
                    .map(entry -> {
                        String lastElement = null;
                        String elementWithSpace = null;
                        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                        Matcher matcher = pattern.matcher(entry.getText());
                        String text = entry.getText();
                        while (matcher.find()) {
                            String element = matcher.group(1);
                            if (element.contains("|")) {
                                List<String> parts = Arrays.stream(element.split("\\|")).toList();
                                for (String part : parts) {

                                    if (part.contains(" ")) {
                                        elementWithSpace = part;
                                        break;
                                    }
                                    lastElement = part;
                                }
                            }

                            if(elementWithSpace != null ) text = text.replace("[" + element + "]", elementWithSpace);
                            else if(lastElement != null ) text = text.replace("[" + element + "]", lastElement);
                        }
                        if(!entry.getText().equals(text)) return text.replace("[", "").replace("]","");
                        else return entry.getText().replace("[", "").replace("]","");

                    })
                    .collect(Collectors.toList());

            combobox.setItems(FXCollections.observableArrayList(texts));
            combobox.setEditable(true);

            var ref = new Object() {
                boolean isc = false;
            };
            final ChangeListener<String> listener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (ref.isc) return;
                    if (!combobox.isShowing()) combobox.hide();
                    ObservableList<String> filteredItems = FXCollections.observableArrayList();
                    for (String item : texts) {
                        if (item.toLowerCase().contains(newValue.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }
                    if (!filteredItems.isEmpty()) {
                        ref.isc = true;
                        combobox.setItems(filteredItems);
                        ref.isc = false;
                        combobox.show();
                    } else {
                        combobox.hide();
                    }
                    if (!ref.isc) {
                        combobox.getEditor().textProperty().removeListener(this);
                        combobox.getEditor().setText(newValue);
                        combobox.getEditor().end();
                        combobox.getEditor().textProperty().addListener(this);
                        combobox.setEditable(true);
                    }
                }
            };

            combobox.getEditor().textProperty().addListener(listener);

            combobox.setOnKeyPressed(event -> handleKeyPressed(event, combobox));

            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(true);

            javafx.scene.control.TextField minField = new javafx.scene.control.TextField();
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
            javafx.scene.control.TextField maxField = new TextField();
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

            Region spacer1 = new Region();
            Region spacer2 = new Region();

            HBox hBox = new HBox(10, checkBox, spacer1, combobox, spacer2, minField, maxField);
            hBox.setStyle("-fx-padding: 5px;");
            hBox.setAlignment(Pos.CENTER_LEFT);

            controller.getVbox().getChildren().add(controller.getVbox().getChildren().size() -1, hBox);

            Button button = findButtonAddMods(controller.getVbox());

            if(sizeChildHbox(controller.getVbox()) > 8 && button != null){
                button.setDisable(true);
            }

        });
        return buttton;
    }

    private void handleKeyPressed(javafx.scene.input.KeyEvent event, ComboBox comboBox) {

            switch (event.getCode()) {
                case BACK_SPACE -> {
                    if(comboBox.getValue() == null) {
                        List<String> texts = StaticData.getInstance().getResults().get(0).getEntries().stream()
                                .map(entry -> {
                                    String lastElement = null;
                                    String elementWithSpace = null;
                                    Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                                    Matcher matcher = pattern.matcher(entry.getText());
                                    String text = entry.getText();
                                    while (matcher.find()) {
                                        String element = matcher.group(1);
                                        if (element.contains("|")) {
                                            List<String> parts = Arrays.stream(element.split("\\|")).toList();
                                            for (String part : parts) {

                                                if (part.contains(" ")) {
                                                    elementWithSpace = part;
                                                    break;
                                                }
                                                lastElement = part;
                                            }
                                        }

                                        if(elementWithSpace != null ) text = text.replace("[" + element + "]", elementWithSpace);
                                        else if(lastElement != null ) text = text.replace("[" + element + "]", lastElement);
                                    }
                                    if(!entry.getText().equals(text)) return text.replace("[", "").replace("]","");
                                    else return entry.getText().replace("[", "").replace("]","");

                                })
                                .collect(Collectors.toList());

                        comboBox.setItems(FXCollections.observableArrayList(texts));
                    }
                }
            }

    }

    private void sendHideoutCommand() {
        try {
            Robot robot = new Robot();

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            for (char c : "/hideout".toCharArray()) {
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            }

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

        } catch (AWTException e) {
            System.err.println(e.getMessage());
        }
    }

    private void showWindowAtCursor() {
        WinDef.POINT p = new WinDef.POINT();

        User32.INSTANCE.GetCursorPos(p);
        double windowWidth = primaryStage.getWidth();
        double windowHeight = primaryStage.getHeight();

        WinDef.RECT gameWindowRect = WindowDetector.getGameWindow("Path of Exile 2");
        if(gameWindowRect == null ) gameWindowRect = WindowDetector.getGameWindow("Path of Exile");
        if(gameWindowRect == null) return;
        double newX = p.x - windowWidth - 10;
        double newY = p.y + 10;

        assert gameWindowRect != null;
        newX = Math.max(gameWindowRect.left, Math.min(newX, gameWindowRect.right - windowWidth));


        newY = Math.max(gameWindowRect.top, Math.min(newY, gameWindowRect.bottom - windowHeight));
        double finalNewX = newX;
        double finalNewY = newY;

        Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.setOpacity(0.9);
            primaryStage.setAlwaysOnTop(true);
            primaryStage.setX(finalNewX);
            primaryStage.setY(finalNewY);
        });
    }
}
