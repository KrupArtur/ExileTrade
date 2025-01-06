package com.demo.poe.Service;

import com.demo.poe.Model.Settings;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TempFile {
    public static File tempFile;

    public static void saveTempFile(String nameFileTemp,String body){
        if(fileTempExists(nameFileTemp)) return;
        try {
            //"staticDataTemp"
            tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + nameFileTemp);
            try(FileWriter writer = new FileWriter(tempFile)){
                writer.write(body);
            }
            //saveFilePath(tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void saveFilePath(String file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean fileTempExists(String nameFileTemp){
       return Files.exists(Paths.get(System.getProperty("java.io.tmpdir") + File.separator + nameFileTemp));
   }


    public static String readTempFile(String nameFileTemp) {
        try {
            return new String(Files.readAllBytes(Paths.get(System.getProperty("java.io.tmpdir") + File.separator + nameFileTemp)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loadSettings() {
        try {
            return new String(Files.readAllBytes(Paths.get("settings.json")));
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveConfig(List<Settings> settingsList) {
        JSONObject json = new JSONObject();
        for (Settings setting: settingsList) {
            json.put(setting.getId(),setting.getValue());
        }

        try {
            Files.write(Paths.get("settings.json"), json.toString(4).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
