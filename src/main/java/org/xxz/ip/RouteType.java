package org.xxz.ip;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jsbxyyx
 */
public final class RouteType {

    public enum RouteTypeEnum {
        TPLINK("tplink"),
        XIAOMI("xiaomi"),
        ;
        public final String key;
        RouteTypeEnum(String key) {
            this.key = key;
        }
    }

    private static final Map<String, RouteTypeEnum> MAP = new HashMap<>();

    static {
        RouteTypeEnum[] values = RouteTypeEnum.values();
        for (RouteTypeEnum value : values) {
            MAP.put(value.key, value);
        }
    }

    public static boolean support(String type) {
        return MAP.containsKey(type);
    }

}
