package com.springboard.projectboard.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Component
public class FileValidator {
    
    private static final Set<String> ALLOWED_EXTENSIONS = 
        Set.of("jpg", "jpeg", "png", "gif", "tiff", "tif", "raw");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILE_COUNT = 5;

    public void validateFiles(List<MultipartFile> files) {
        validateFiles(files, 0);
    }

    public void validateFiles(List<MultipartFile> files, int existingFileCount) {
        if (files == null || files.isEmpty()) {
            return;
        }

        int newFileCount = (int) files.stream().filter(file -> !file.isEmpty()).count();
        int totalFileCount = existingFileCount + newFileCount;

        if (totalFileCount > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("최대 " + MAX_FILE_COUNT + "개의 파일만 업로드 가능합니다. (기존: " + existingFileCount + "개, 새 파일: " + newFileCount + "개)");
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            return;
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다: " + file.getOriginalFilename());
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + originalFilename);
        }
    }

    private boolean hasValidExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return false;
        }
        
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}