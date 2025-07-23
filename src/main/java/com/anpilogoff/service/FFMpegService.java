package com.anpilogoff.service;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class FFMpegService {
    private final ExecutorService ffmpegOutputReaderExecutor = Executors.newFixedThreadPool(2);

    public String convertThenSegment(String fileName, String bitrate) throws IOException {
        File inputFile = new File(fileName);
        Path outputDir;
        try {
            outputDir = Paths.get(fileName.replace(".flac", ""));
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            log.error("Cannot create output directory", e);
            throw new RuntimeException(e);
        }

        String[] cmd = {
                "/home/iam/bin/ffmpeg",
                "-i", inputFile.getAbsolutePath(),
                "-map", "0:a",
                "-c:a", "libfdk_aac",
                "-vbr", "5",//"-b:a", bitrate,
                "-ac", "2",
                "-ar", "44100",
                "-f", "hls",
                "-hls_time", "5",
                "-hls_segment_type", "mpegts",
                "-hls_list_size", "0",
                "-threads", "2",
                "-loglevel", "error",
                "-hls_segment_filename", outputDir.resolve("segment_%03d.ts").toString(),
                outputDir.resolve("playlist.m3u8").toString()
        };

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = null;
        Future<?> stdoutFuture = null;
        Future<?> stderrFuture = null;

        try {
            process = pb.start();

            //запуск чтения выводов
            stdoutFuture = readOutputStream(process);
            stderrFuture = readErrorStream(process);

            int exitCode = process.waitFor();
            if (exitCode != 0) log.error("FFmpeg failed with exit code {}", exitCode);

            // Проверяем существование плейлиста
            File playlist = outputDir.resolve("playlist.m3u8").toFile();
            if (!playlist.exists()) log.error("HLS playlist not created");

            Files.deleteIfExists(Paths.get(fileName));

            return outputDir.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("FFmpeg process was interrupted{}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error during FFmpeg execution{}", e.getMessage(), e);
        } finally {
            // Гарантированно завершаем процесс и потоки чтения
             if (process != null) process.destroy();
             if (stdoutFuture != null) stdoutFuture.cancel(true);
             if (stderrFuture != null) stderrFuture.cancel(true);
        }
        return outputDir.toString();
    }

    private Future<?> readOutputStream(Process process) {
        return ffmpegOutputReaderExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[ffmpeg stdout] {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading ffmpeg stdout", e);
            }
        });
    }

    private Future<?> readErrorStream(Process process) {
        return ffmpegOutputReaderExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[ffmpeg stderr] {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading ffmpeg stderr", e);
            }
        });
    }

    public void shutdown() {
        ffmpegOutputReaderExecutor.shutdownNow();
    }
}