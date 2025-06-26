package com.anpilogoff.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpUtil {
    public static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    public static HttpRequest buildGetRequest(String url) {
        String auth_token = ConfigUtil.loadConfig("env.properties").getProperty("AUTH_TOKEN");
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-User-Auth-Token", auth_token)
                .GET()
                .build();
    }

    public static String downloadFile(String url, String fileName)  {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        log.info("Downloading file {}", fileName);
        InputStream inputStream = null;

        try {
            inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (IOException | InterruptedException e) {
            log.info("Error during audio file downloading {} : \n {}", fileName, e.getMessage(),e);
            return null;
        }

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(fileName))) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            inputStream.close();
        }catch (Exception e){
            log.error("Error during audio file data writing: {} \n      {}", fileName, e.getMessage());
            return null;
        }
        return fileName;
    }
}
