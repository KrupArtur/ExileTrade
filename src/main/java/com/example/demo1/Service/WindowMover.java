package com.example.demo1.Service;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class WindowMover {
    public static void moveWindow(String windowTitle, int x, int y) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd == null) {
            System.out.println("Window not found: " + windowTitle);
            return;
        }

        User32.INSTANCE.SetWindowPos(hwnd, null, x, y, 0, 0, User32.SWP_NOSIZE | User32.SWP_NOZORDER);
    }
}
