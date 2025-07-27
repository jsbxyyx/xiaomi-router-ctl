package org.xxz.ip;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jsbxyyx
 */
public class RouteConfigFactory {

    private static final RouteConfigFactory INSTANCE = new RouteConfigFactory();
    private static JsonObject config;

    private RouteConfigFactory() {
    }

    public static RouteConfigFactory getInstance(JsonObject applicationJson) {
        if (config == null) {
            config = applicationJson;
        }
        return INSTANCE;
    }

    public RouteConfig create(String type) {
        if (!RouteType.support(type)) {
            throw new RuntimeException("not support " + type);
        }
        RouteConfig c = new RouteConfig();
        JsonObject obj = config.get(type).getAsJsonObject();

        Map<String, Object> map = new Gson().fromJson(obj, new TypeToken<Map<String, Object>>(){}.getType());

        c.pwd = map.get("pwd").toString();
        c.routeURL = map.get("routeURL").toString();
        c.extra = map;
        return c;
    }

}
