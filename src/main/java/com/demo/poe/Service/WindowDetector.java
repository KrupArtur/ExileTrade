package com.demo.poe.Service;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.*;

public class WindowDetector {
    public static RECT getGameWindow(String windowTitle) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd == null) {
            System.out.println("Window not found!");
            return null;
        }

        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        return rect;
    }

    public static boolean isPoEActive() {
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        char[] windowTitle = new char[512];

        User32.INSTANCE.GetWindowText(hwnd, windowTitle, 512);

        String title = new String(windowTitle);

        return title.contains("Path of Exile") || title.contains("PoE Item");
    }
}
