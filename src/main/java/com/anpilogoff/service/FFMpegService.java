package com.anpilogoff.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FFMpegService {

    public String convertThenSegment(String fileName, String bitrate) {

            try {
                File inputFile = new File(fileName);

                // 1. Подготовка папки вывода
                Path outputDir = Paths.get("dir"+fileName);
                Files.createDirectories(outputDir);

                // 2. Команда FFmpeg
                String[] cmd = {
                        "ffmpeg",
                        "-i", inputFile.getAbsolutePath(),
                        "-c:a", "libfdk_aac",
                        "-b:a", bitrate,
                        "-f", "segment",
                        "-segment_time", "7",
                        "-segment_list", outputDir.resolve("playlist.m3u8").toString(),
                        outputDir.resolve("segment_%03d.ts").toString()
                };

                // 3. Запуск и обработка
                Process process = new ProcessBuilder(cmd).start();
                int exitCode = process.waitFor();
                process.destroy();

                if( exitCode !=  0){
                    throw new Exception("ffmpeg process failed with exit code " + exitCode);
                }

                log.info("Segmentation completed successfully: {}", exitCode);

                // 4. Удаление временного файла
                if(inputFile.delete()){
                    log.info("FFMPEG: input file deleted ");
                }
                return outputDir.toString();
            } catch (Exception e) {
                log.warn("Ошибка обработки {}: ", fileName);
                e.printStackTrace();
            }
            return null;
    }

}
