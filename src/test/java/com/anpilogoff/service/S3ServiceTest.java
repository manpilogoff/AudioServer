package com.anpilogoff.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3ServiceTest {
    @Mock private AmazonS3 mockS3;
    @Mock private ExecutorService mockExecutor;
    private S3Service s3Service;
    private File tempDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        s3Service = new S3Service(mockS3, mockExecutor);
        tempDir = Files.createTempDirectory("s3test").toFile();
    }

    @AfterEach
    void tearDown() {
        Arrays.stream(tempDir.listFiles()).forEach(File::delete);
        tempDir.delete();
    }

    @Test
    void uploadFolder_Success() throws IOException {
        // Подготовка тестовых файлов
        File file1 = new File(tempDir, "file1.ts");
        File file2 = new File(tempDir, "file2.ts");
        Files.write(file1.toPath(), "data".getBytes());
        Files.write(file2.toPath(), "data".getBytes());

        // Мокируем ExecutorService для немедленного выполнения задач
        doAnswer(inv -> {
            Runnable task = inv.getArgument(0);
            task.run();
            return null;
        }).when(mockExecutor).submit(any(Runnable.class));

        // Мокируем успешную загрузку
        when(mockS3.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());

        assertTrue(s3Service.uploadFolderToS3("bucket", "prefix/", tempDir));
        verify(mockS3, times(2)).putObject(any());
    }

    @Test
    void uploadFileWithRetry_SuccessAfterRetry() {
        when(mockS3.putObject(any())).thenReturn(new PutObjectResult());

        File testFile = new File("test.ts");
        boolean result = s3Service.uploadToS3("bucket", "key", testFile);

        verify(mockS3, times(1)).putObject(any());
        assertTrue(result);
    }

    @Test
    void uploadToS3_PermanentFailure() {
        File testFile = new File("test.ts");

        when(mockS3.putObject(any())).thenThrow(new AmazonClientException("Permanent error"));

        boolean result = s3Service.uploadToS3("bucket", "key", testFile);

        assertFalse(result);
        verify(mockS3, times(3)).putObject(any());
    }
}