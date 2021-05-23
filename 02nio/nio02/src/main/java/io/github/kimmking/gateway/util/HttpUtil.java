package io.github.kimmking.gateway.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: boss-crm
 * @description:
 * @author: Yu Bin
 * @create: 2021-05-17 11:47
 **/
public class HttpUtil {
    private static int socketTimeout = 2000;

    private static int connectTimeout = 2000;

    private static int connectionRequestTimeout = 2000;

    public static CloseableHttpClient createCloseableHttpClient() {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    public static String getJsonByApi(String url) throws IOException {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        try {
            httpGet = new HttpGet(url);
            response = HttpUtil.createCloseableHttpClient().execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return StringUtils.EMPTY;
            }
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (response != null) {
                response.close();
            }
        }
    }

    public static String postJsonByApi(String url, Map<String, String> param) throws UnsupportedEncodingException, IOException {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setEntity(getEntity(param));
            response = HttpUtil.createCloseableHttpClient().execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return StringUtils.EMPTY;
            }
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            if (response != null) {
                response.close();
            }
        }
    }

    private static HttpEntity getEntity(Map<String, String> paramMap) throws UnsupportedEncodingException {
        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            nvps.add(new BasicNameValuePair(key, value));
        }
        return new UrlEncodedFormEntity(nvps, "UTF-8");
    }

    public static void main(String[] args) {
        try {
            System.out.println(HttpUtil.getJsonByApi("http://localhost:8088/api/hello"));
            Map<String, String> param = new HashMap<>();
            param.put("key1", "yubin");
            System.out.println(HttpUtil.postJsonByApi("http://localhost:8088/api/hello", param));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
