package org.xxz.ip;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xxz.ip.util.AwsUtil;
import org.xxz.ip.util.FileUtil;
import org.xxz.ip.util.IOUtil;
import org.xxz.ip.util.WxUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jsbxyyx
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        JsonObject applicationJson = IOUtil.readConfig();
        if (applicationJson == null) {
            log.error("applicationJson is null");
            return;
        }

        String routeType = applicationJson.get("routeType").getAsString();
        RouteConfig config = RouteConfigFactory.getInstance(applicationJson).create(routeType);
        String ipaddr = Xiaomi.loginWithFetchIP(config.routeURL, config);
        log.info("ipaddr={}", ipaddr);

        boolean equals = FileUtil.compareAndSet("ip", ipaddr);
        if (!equals) {
            log.info("update dns record. {}", ipaddr);
            WxUtil.getInstance().updateDomainRecord(ipaddr);
            AwsUtil.getInstance().updateDomainRecord(ipaddr);
        }

        limit(config);

    }

    private static void limit(RouteConfig config) {

        long currentTime = new Date().getTime();

        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        List<Map<String, Object>> limitList = (List<Map<String, Object>> ) config.extra.get("limit");
        for (Map<String, Object> map : limitList) {
            List<List<String>> times = (List<List<String>>) map.get("time");
            String mac = (String) map.get("mac");
            List<String> updown = (List<String>) map.get("updown");
            boolean enable = (boolean) map.get("enable");
            log.info("map:{}", map);

            boolean exists = false;
            for (List<String> time : times) {
                String timeMin = todayString + " " + time.get(0);
                String timeMax = todayString + " " + time.get(1);
                try {
                    long min = sdf.parse(timeMin).getTime();
                    long max = sdf.parse(timeMax).getTime();
                    if (min <= currentTime && currentTime <= max) {
                        exists = true;
                        break;
                    }
                } catch (ParseException e) {
                    log.error("parse time error.", e);
                }
            }

            if (exists && enable) {
                log.info("limit. mac:{} updown:{}", mac, updown);
                // 执行限流
                Xiaomi.limit(config.routeURL, config, Arrays.asList(mac), updown.get(1), updown.get(0));
            } else {
                log.info("unlimit. mac:{}", mac);
                // 解除限流
                Xiaomi.limit(config.routeURL, config, Arrays.asList(mac), "0", "0");
            }
        }

    }

}
