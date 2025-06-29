
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

public class HttpUtil {
        private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
        private static final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        public static HttpRequest buildGetRequest(String url) {
            String auth_token = ConfigUtil.loadConfig("env.properties").getProperty("AUTH_TOKEN");
            return HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-User-Auth-Token", auth_token)
                    .GET()
                    .build();
        }

        /**
         * Асинхронная загрузка файла
         * @param url URL для загрузки
         * @param outputFileName имя выходного файла
         * @return Future с путем к скачанному файлу
         */
        public static String downloadFileAsync(String url, String outputFileName, String outFileFormat) {

                Path outputPath = Paths.get(outputFileName+outFileFormat);
                try {
                    HttpRequest request = buildGetRequest(url);

                    log.info("Начало загрузки: {} -> {}", url, outputFileName);
                    long startTime = System.currentTimeMillis();

                    try (InputStream inputStream = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
                         ReadableByteChannel inChannel = Channels.newChannel(inputStream);
                         FileChannel outChannel = FileChannel.open(outputPath,
                                 StandardOpenOption.WRITE,
                                 StandardOpenOption.CREATE,
                                 StandardOpenOption.TRUNCATE_EXISTING)) {

                        ByteBuffer buffer = ByteBuffer.allocateDirect(256 * 1024); // 1MB buffer
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
    }