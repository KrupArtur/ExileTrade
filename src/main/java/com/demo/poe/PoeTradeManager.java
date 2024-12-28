package com.demo.poe;

import com.demo.poe.Service.WindowDetector;
import com.sun.jna.platform.win32.WinDef;

public class PoeTradeManager {
    WinDef.RECT rect =  WindowDetector.getGameWindow("Path of Exile 2");
}
