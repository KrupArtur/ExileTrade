package com.example.demo1.Events;

import com.example.demo1.Controller.MainWindowController;
import com.example.demo1.Service.ClipboardContent;
import com.example.demo1.Service.WindowDetector;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.KeyEvent;

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
        if (!isWindowVisible && nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_C && isCtrlPressed(nativeKeyEvent)) {
            Platform.runLater(this::toggleWindowVisibility);
        } else if( !isWindowVisible &&  nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_D && isCtrlPressed(nativeKeyEvent)) {
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

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    private boolean isCtrlPressed(NativeKeyEvent nativeKeyEvent) {
        return (nativeKeyEvent.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
    }

    private void toggleWindowVisibility() {
        String newClipbordContent = ClipboardContent.getClipboardContent();
        controller.getVbox().getChildren().clear();
        controller.addMods(controller.getVbox());
        controller.clearTable();

        if (isWindowVisible && newClipbordContent.equals(lastClipbordContent)) {
            Platform.runLater(primaryStage::hide);
            lastClipbordContent = "";
            isWindowVisible = false;
        } else {
            lastClipbordContent = newClipbordContent;
            showWindowAtCursor();
            isWindowVisible = true;
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

        double newX = p.x - windowWidth - 10;
        double newY = p.y + 10;

        assert gameWindowRect != null;
        newX = Math.max(gameWindowRect.left, Math.min(newX, gameWindowRect.right - windowWidth));


        newY = Math.max(gameWindowRect.top, Math.min(newY, gameWindowRect.bottom - windowHeight));
        double finalNewX = newX;
        double finalNewY = newY;
        Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.setOpacity(0.9);  // Możesz dostosować widoczność
            primaryStage.setAlwaysOnTop(true); // Zawsze na wierzchu
            primaryStage.setX(finalNewX);
            primaryStage.setY(finalNewY);
        });
    }
}
