
package com.anpilogoff.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Optional;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public static HttpRequest buildGetRequest(String url) {
        String auth_token = ConfigUtil.getProperty("env.properties","AUTH_TOKEN");
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-User-Auth-Token", auth_token)
                .GET()
                .build();
    }

    public static HttpRequest buildGetRequestWithAlternativeAuthToken(String url) {
        System.out.println("buildGetRequestWithAlternativeAuthToken");
        String auth_token = ConfigUtil.getProperty("env.properties","AUTH_TOKEN_ALTERNATIVE");
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-User-Auth-Token", auth_token)
                .GET()
                .build();
    }

    public static HttpRequest buildHeadRequest(String url) {
        // Proper way to do HEAD request
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-User-Auth-Token", ConfigUtil.getProperty("env.properties","AUTH_TOKEN"))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())  // Proper way to do HEAD request
                .build();
    }

    /**
     * Асинхронная загрузка файла
     * @param url URL для загрузки
     * @param outputFileName имя выходного файла
     * @return Future с путем к скачанному файлу
     */
    public static String downloadFileAsync(String url, String outputFileName, String outFileFormat, HttpRequest httpRequest) {

        Path outputPath = Paths.get(outputFileName+outFileFormat);
        try {
            log.info("Начало загрузки: {} -> {}", url, outputFileName);
            long startTime = System.currentTimeMillis();

            try (InputStream inputStream = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream()).body();
                 ReadableByteChannel inChannel = Channels.newChannel(inputStream);
                 FileChannel outChannel = FileChannel.open(outputPath,
                         StandardOpenOption.WRITE,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {

                ByteBuffer buffer = ByteBuffer.allocateDirect(512 * 1024); // 1MB buffer
                while (inChannel.read(buffer) != -1) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        outChannel.write(buffer);
                    }
                    buffer.clear();
                }

                long duration = System.currentTimeMillis() - startTime;
                log.info("Загрузка завершена: {} ({} мс, {} байт)",
                        outputFileName, duration, Files.size(outputPath));

                return outputPath.toString();
            }
        } catch (Exception e) {
            log.error("Ошибка загрузки {}: {}", outputFileName, e.getMessage());
            try {
                Files.deleteIfExists(outputPath);
            } catch (IOException ioException) {
                log.warn("Не удалось удалить частично загруженный файл: {}", outputPath);
            }
            throw new RuntimeException("Ошибка загрузки файла", e);
        }

    }

    // столкнулся с нюансом при скачивании и поиске аудио записи
    // AUTH_TOKEN по причине которую узнать так и не удалось может подходить либо для скачивания либо для поиска
    //на этот случай добавил в env.properties параметр - SEARCH_TOKEN
    //данный метод выполняет HEAD запрос для получения размера трека который нужно скачать( по умолчанию .flac)
    //если размер меньше 10Мб - значит скачан будет preview (длительность 0:30)
    //в таком случае
    public static HttpRequest checkTokenValidForDownload(String url) {
        long fileSize = 0;

        //HEAD type request to check response content length
        HttpRequest request =  HttpUtil.buildHeadRequest(url);

        HttpResponse<Void> response;

        try { response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding()); }
        catch (IOException | InterruptedException e) { throw new RuntimeException(e); }

        Optional<String> contentLength = response.headers().firstValue("Content-Length");

        if (contentLength.isPresent()) { fileSize = Long.parseLong( contentLength.get()); }
        else { log.info("Content-Length header not available"); }

        if (fileSize < 10000000) {
            System.out.println("alternative");
            return HttpUtil.buildGetRequestWithAlternativeAuthToken(url); }

        return HttpUtil.buildGetRequest(url);
    }

    // столкнулся с нюансом при скачивании и поиске аудио записи
    // AUTH_TOKEN по причине которую узнать так и не удалось может подходить либо для скачивания либо для поиска
    //на этот случай добавил в env.properties параметр - AUTH_TOKEN_ALTERNATIVE
    public static HttpRequest checkSearchResponseValidWithAuthToken(String url) {
        HttpRequest request = HttpUtil.buildGetRequest(url);
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (JsonUtil.extractSearch(response.body()).length() < 50) {
                request =  HttpUtil.buildGetRequestWithAlternativeAuthToken(url);
            }
        } catch (NullPointerException | IOException | InterruptedException e) {
            log.error("Error while CLIENT.s{}", e.getMessage(), e);
            return null;
        }
        return request;
    }

}