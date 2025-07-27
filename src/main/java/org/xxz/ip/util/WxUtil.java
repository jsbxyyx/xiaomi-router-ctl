package org.xxz.ip.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

import static org.xxz.ip.util.HttpClient.lists;
import static org.xxz.ip.util.HttpClient.post;

public class WxUtil {

    private static final Logger log = LoggerFactory.getLogger(AliyunUtil.class);

    private static final WxUtil INSTANCE = new WxUtil();

    private final String webhook;
    private final JsonObject config;

    private WxUtil() {
        config = IOUtil.readConfig();
        webhook = config.get("wework-webhook").getAsString();
    }

    public static WxUtil getInstance() {
        return INSTANCE;
    }

    public boolean updateDomainRecord(String ipaddr) {
        send(Base64.getEncoder().encodeToString(ipaddr.getBytes()));
        return true;
    }

    public void send(String msg) {
        JsonObject content = new JsonObject();
        content.addProperty("msgtype", "text");
        JsonObject text = new JsonObject();
        text.addProperty("content", msg);
        content.add("text", text);
        HttpClient.HttpResult result = post(webhook,
                lists("content-type", "application/json"),
                new Gson().toJson(content));
        log.info("send result : {}", result);
    }



}
