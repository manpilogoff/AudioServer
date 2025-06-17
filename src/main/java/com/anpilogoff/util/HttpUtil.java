package com.anpilogoff.util;

import java.net.URI;
import java.net.http.HttpRequest;

public class HttpUtil {
    public static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent","Mozilla/5.8")
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    public static HttpRequest buildSignedGetRequest(String url) {
return null;
    }
}
