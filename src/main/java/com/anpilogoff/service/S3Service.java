package com.anpilogoff.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.anpilogoff.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class S3Service {
    private final AmazonS3 s3Client;
    private final ExecutorService uploadExecutor;
    private static final String S3_CONFIG = "s3config.properties";

    public S3Service(AmazonS3 s3Client, ExecutorService uploadExecutor) {
        this.s3Client = s3Client;
        this.uploadExecutor = uploadExecutor;
    }

    public static S3Service create(ExecutorService uploadExecutor) {
        BasicAWSCredentials creds = new BasicAWSCredentials(
                ConfigUtil.getProperty(S3_CONFIG, "S3_ACCESS_KEY"),
                ConfigUtil.getProperty(S3_CONFIG, "S3_SECRET_KEY"));

                AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                        .withPathStyleAccessEnabled(true)
                        .withCredentials(new com.amazonaws.auth.AWSStaticCredentialsProvider(creds))
                        .withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(
                        "https://s3.regru.cloud", "ru-central-1"))
                        .build();
                return new S3Service(s3client, uploadExecutor);
    }

    public boolean uploadToS3(String bucketName, String objectKey, File file) { //или просто key?
        try {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, file);
            PutObjectResult putResult = s3Client.putObject(putRequest);

            log.debug("Uploaded: {} | ETag: {}", objectKey, putResult.getETag());
            return true;
        } catch (AmazonServiceException ex) {
            log.error("Failed to upload file", ex);
            return false;
        }
    }


    public boolean uploadFolderToS3(String bucketName, String s3FolderPath, File localFolder) {
        if (!localFolder.isDirectory()) {
            log.error("Not a directory: {}", localFolder.getAbsolutePath());

            return false;
        }

        File[] files = localFolder.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            log.warn("Empty directory: {}", localFolder);

            return true;
        }

        CountDownLatch latch = new CountDownLatch(files.length);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (File file : files) {
            uploadExecutor.submit(() -> {
                log.debug("--Uploading file: {}", file.getName());

                String key = s3FolderPath + "/" +file.getName();

                if (uploadToS3(bucketName, key, file)) {
                    successCount.incrementAndGet();

                    log.debug("-Successfully uploaded file: {}", file.getName());
                } else {
                    failureCount.incrementAndGet();

                    log.debug("-Failed to upload file: {}", file.getName());
                }
               latch.countDown();
            });
        }


        try {
            if(!latch.await(10, TimeUnit.SECONDS)) {
                log.warn("Timeout waiting downloads completion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Uploads interrupted: {}", e.getMessage());
        }

        log.info("ALL FILES SUCCESSFULLY uploaded {} files", successCount.get());
        return successCount.get() == files.length;
    }



}
