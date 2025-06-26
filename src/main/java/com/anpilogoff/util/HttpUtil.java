package com.anpilogoff.util;

import java.net.URI;
import java.net.http.HttpRequest;

public class HttpUtil {
    public static HttpRequest buildGetRequest(String url) {
        String auth_token = ConfigUtil.loadConfig("env.properties").getProperty("AUTH_TOKEN");
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-User-Auth-Token", auth_token)
                .GET()
                .build();
    }
}
