package org.xxz.ip.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class FileUtil {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static synchronized boolean compareAndSet(String filename, String content) {
        File f = new File(TMP_DIR + "/" + filename);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ignore) {
            }
        }
        try {
            String str = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            boolean equals = Objects.equals(str, content);
            if (equals) {
                return true;
            }
            Files.write(f.toPath(), content.getBytes());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
