package com.example.demo1.Service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class LockApplication {
    public static boolean lockApplication(){
        String userHome = System.getProperty("user.home");
        File file = new File(userHome, "my.lock");
        try {
            FileChannel fc = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            FileLock lock = fc.tryLock();

            return lock == null;
        } catch (IOException e) {
            throw new Error(e);
        }
    }

}
