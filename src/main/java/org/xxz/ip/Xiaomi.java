package org.xxz.ip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xxz.ip.util.SimpleHttpClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tt
 */
public class Xiaomi {

    private static final Logger log = LoggerFactory.getLogger(Xiaomi.class);

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static String login(String routeURL, RouteConfig config) {
        String mac = config.extra.get("mac").toString();
        String key = config.extra.get("key").toString();
        String type = "0";
        String time = String.valueOf(new Date().getTime() / 1000);
        String random = String.valueOf(Math.floor(Math.random() * 10000));
        String nonce = type + "_" + mac + "_" + time + "_" + random;

        String username = "admin";
        String password = SHA1(nonce + SHA1(config.pwd + key));
        String logtype = "2";

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("logtype", logtype));
        parameters.add(new BasicNameValuePair("nonce", nonce));
        SimpleHttpClient.HttpResult httpResult = SimpleHttpClient.httpPost(routeURL + "/cgi-bin/luci/api/xqsystem/login",
                new ArrayList<>(), parameters);
        String content = httpResult.getContent();
        JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        int code = json.get("code").getAsInt();
        if (code != 0) {
            return null;
        }
        // {"url":"/cgi-bin/luci/;stok=9b125dbab6fbcb9485db5d3cb4d53cb2/web/home","token":"9b125dbab6fbcb9485db5d3cb4d53cb2","code":0}
        String token = json.get("token").getAsString();
        return token;
    }

    public static String loginWithFetchIP(String routeURL, RouteConfig config) {

        String token = login(routeURL, config);

        SimpleHttpClient.HttpResult httpResult1 = SimpleHttpClient.httpGet(routeURL + "/cgi-bin/luci/;stok=" + token + "/api/xqnetwork/wan_info", new ArrayList<>());
        String content1 = httpResult1.getContent();
        JsonObject json1 = JsonParser.parseString(content1).getAsJsonObject();
        String ipaddr = json1.get("info").getAsJsonObject()
                .get("ipv4").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("ip").getAsString();
        return ipaddr;
    }

    public static void limit(String routeURL, RouteConfig config, List<String> macs, String maxdown, String maxup) {
        String token = login(routeURL, config);
        JsonArray data = new JsonArray();;
        for (String mac : macs) {
            if (mac != null && !"".equals(mac)) {
                JsonObject object = new JsonObject();
                object.addProperty("mac", mac);
                object.addProperty("maxup", maxup);
                object.addProperty("maxdown", maxdown);
                data.add(object);
            }
        }

        if (data.size() > 0) {
            List<BasicNameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("data", gson.toJson(data)));
            SimpleHttpClient.HttpResult httpResult = SimpleHttpClient.httpPost(routeURL + "/cgi-bin/luci/;stok=" + token + "/api/misystem/qos_limits",
                    new ArrayList<>(), parameters);
            String content = httpResult.getContent();
            log.info("qos_limits resp : " + content);
        }

    }

    public static String SHA1(String str){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(str.getBytes());
            byte[] messageDigest = digest.digest();
            return byteArrayToHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String byteArrayToHex(byte [] a) {
        int hn, ln, cx;
        String hexDigitChars = "0123456789abcdef";
        StringBuffer buf = new StringBuffer(a.length * 2);
        for(cx = 0; cx < a.length; cx++) {
            hn = ((int)(a[cx]) & 0x00ff) /16 ;
            ln = ((int)(a[cx]) & 0x000f);
            buf.append(hexDigitChars.charAt(hn));
            buf.append(hexDigitChars.charAt(ln));
        }
        return buf.toString();
    }

}
