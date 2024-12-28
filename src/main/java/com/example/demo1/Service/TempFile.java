package com.example.demo1.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TempFile {
    public static File tempFile;

    public static void saveTempFile(String nameFileTemp,String body){
        if(fileTempExists()) return;
        try {
            //"staticDataTemp"
            tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "staticDataTemp.json");
            try(FileWriter writer = new FileWriter(tempFile)){
                writer.write(body);
            }
            saveFilePath(tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void saveFilePath(String filePath) {
        try (FileWriter writer = new FileWriter("tempFilePath.txt")) {
            writer.write(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean fileTempExists(){
        String path = getTempFilePath();
        if(path != null ) {
            return Files.exists(Paths.get(path));
        }
        return false;
    }

    public static String getTempFilePath() {
        try {
            String filePath = new String(Files.readAllBytes(Paths.get("tempFilePath.txt")));
            return filePath.trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readTempFile() {
        try {
            String path = getTempFilePath();
            if (path != null) {
                return new String(Files.readAllBytes(Paths.get(path)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
