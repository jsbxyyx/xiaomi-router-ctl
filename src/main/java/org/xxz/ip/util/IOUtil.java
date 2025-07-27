package org.xxz.ip.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author tt
 */
public final class IOUtil {

    private static final Logger log = LoggerFactory.getLogger(IOUtil.class);

    public static String toString(InputStream input) {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int n;
        try {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException ignore) {
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    public static JsonObject readConfig() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.json")) {
            String s = IOUtil.toString(in);
            return JsonParser.parseString(s).getAsJsonObject();
        } catch (IOException e) {
            log.error("load application.json error", e);
        }
        return null;
    }

}
