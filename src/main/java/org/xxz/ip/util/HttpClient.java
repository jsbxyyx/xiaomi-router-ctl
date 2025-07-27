package org.xxz.ip.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author jsbxyyx
 */
public class HttpClient {

    private static final int CONNECT_TIMEOUT = Integer.parseInt(System.getProperty("http.connect.timeout", "30000"));
    private static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("http.read.timeout", "30000"));

    public static List<String> lists(String... strings) {
        List<String> list = new ArrayList<>();
        if (strings != null && strings.length > 0) {
            for (String string : strings) {
                list.add(string);
            }
        }
        return list;
    }

    public static HttpResult get(String u, List<String> headers) {
        HttpURLConnection con = null;
        InputStream in = null;
        try {
            URL url = new URL(u);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);
            if (headers != null && !headers.isEmpty()) {
                for (int i = 0; i < headers.size(); ) {
                    con.addRequestProperty(headers.get(i++), headers.get(i++));
                }
            }
            int status = con.getResponseCode();

            if (status > 299) {
                in = con.getErrorStream();
            } else {
                in = con.getInputStream();
            }

            int contentLength = con.getContentLength();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(contentLength);
            byte[] buf = new byte[1 << 12];
            int n;
            while ((n = in.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return new HttpResult(status, baos.toByteArray(), con.getHeaderFields());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    public static HttpResult post(String u, List<String> headers, String parameters) {
        HttpURLConnection con = null;
        InputStream in = null;
        try {
            URL url = new URL(u);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);
            if (headers != null && !headers.isEmpty()) {
                for (int i = 0; i < headers.size(); ) {
                    con.addRequestProperty(headers.get(i++), headers.get(i++));
                }
            }
            byte[] data = parameters.getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Length", String.valueOf(data.length));
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.write(data);
            out.flush();
            out.close();

            int status = con.getResponseCode();

            if (status > 299) {
                in = con.getErrorStream();
            } else {
                in = con.getInputStream();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1 << 12];
            int n;
            while ((n = in.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return new HttpResult(status, baos.toByteArray(), con.getHeaderFields());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    public static String encodingParams(List<String> paramValues, String encoding) {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }

        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext(); ) {
            sb.append(iter.next()).append('=');
            try {
                sb.append(URLEncoder.encode(iter.next(), encoding));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            if (iter.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    public static class HttpResult {
        private final int code;
        private final byte[] content;
        private final Map<String, List<String>> headers;

        public HttpResult(int code, byte[] content, Map<String, List<String>> headers) {
            this.code = code;
            this.content = content;
            this.headers = headers;
        }

        @Override
        public String toString() {
            return "HttpResult{" +
                    "code=" + code +
                    ", content=" + new String(content, StandardCharsets.UTF_8) +
                    ", headers=" + headers +
                    '}';
        }
    }

}
