package org.xxz.ip.util;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author tt
 */
public class SimpleHttpClient {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpClient.class);

    private static RequestConfig config = RequestConfig.custom().setRedirectsEnabled(false).build();

    public static HttpResult httpGet(String url, List<Header> headers) {
        HttpResult result = new HttpResult();
        try {
            // 根据地址获取请求
            HttpGet request = new HttpGet(url);
            if (headers != null) {
                for (Header header : headers) {
                    request.addHeader(header);
                }
            }
            request.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
            HttpResponse response = httpClient.execute(request);
            result = buildHttpResult(response);
        } catch (IOException e) {
            log.error("GET [{}] failed. result [{}]", url, result, e);
        }
        return result;
    }

    public static HttpResult httpPost(String url, List<Header> headers, List<? extends NameValuePair> parameters) {
        HttpResult result = new HttpResult();
        try {
            HttpPost request = new HttpPost(url);
            if (headers != null) {
                for (Header header : headers) {
                    request.addHeader(header);
                }
            }
            request.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
            Header contentType = request.getFirstHeader("Content-Type");
            if (contentType != null && contentType.getValue().startsWith("application/json")) {
                if (parameters != null && !parameters.isEmpty()) {
                    String value = parameters.get(0).getValue();
                    request.setEntity(new StringEntity(value));
                }
            } else {
                request.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
            }
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
            HttpResponse response = httpClient.execute(request);
            result = buildHttpResult(response);
        } catch (IOException e) {
            log.error("POST [{}] parameters [{}] failed. result [{}]", url, parameters, result, e);
        }
        return result;
    }

    private static HttpResult buildHttpResult(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        String content = null;
        if (statusCode == HttpStatus.SC_OK) {
            content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            content = response.getFirstHeader("Location").getValue();
        }
        return new HttpResult(statusCode, null, content);
    }

    public static class HttpResult {
        private int statusCode;
        private String cookie;
        private String content;

        public HttpResult() {
        }

        public HttpResult(int statusCode, String cookie, String content) {
            this.statusCode = statusCode;
            this.cookie = cookie;
            this.content = content;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getCookie() {
            return cookie;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "HttpResult{" +
                    "cookie='" + cookie + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }

}
