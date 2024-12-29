package com.demo.poe.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
