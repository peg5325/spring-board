package com.springboard.projectboard.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3FileUploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, Long articleId, int displayOrder) {
        String originalFilename = file.getOriginalFilename();
        String s3Key = generateS3Key(articleId, displayOrder, originalFilename);

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, s3Key, file.getInputStream(), metadata);
            return amazonS3.getUrl(bucketName, s3Key).toString();
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", originalFilename, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucketName, s3Key);
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", s3Key, e);
        }
    }

    public InputStream downloadFile(String s3Key) {
        try {
            return amazonS3.getObject(bucketName, s3Key).getObjectContent();
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", s3Key, e);
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }

    private String generateS3Key(Long articleId, int displayOrder, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return String.format("articles/%d/%d_%s.%s", articleId, displayOrder, uuid, extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }
}