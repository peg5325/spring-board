package com.springboard.projectboard.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("비즈니스 로직 - 파일 검증")
class FileValidatorTest {

    private final FileValidator sut = new FileValidator();

    @DisplayName("유효한 파일들이 주어지면, 검증을 통과한다")
    @Test
    void givenValidFiles_whenValidating_thenPasses() {
        // Given
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("file2", "test2.png", "image/png", "content2".getBytes())
        );

        // When & Then
        assertThatCode(() -> sut.validateFiles(files))
                .doesNotThrowAnyException();
    }

    @DisplayName("파일 개수가 5개를 초과하면, 예외를 던진다")
    @Test
    void givenTooManyFiles_whenValidating_thenThrowsException() {
        // Given
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "content2".getBytes()),
                new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "content3".getBytes()),
                new MockMultipartFile("file4", "test4.jpg", "image/jpeg", "content4".getBytes()),
                new MockMultipartFile("file5", "test5.jpg", "image/jpeg", "content5".getBytes()),
                new MockMultipartFile("file6", "test6.jpg", "image/jpeg", "content6".getBytes())
        );

        // When & Then
        assertThatThrownBy(() -> sut.validateFiles(files))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 5개의 파일만 업로드 가능합니다");
    }

    @DisplayName("기존 파일과 새 파일 합쳐서 5개를 초과하면, 예외를 던진다")
    @Test
    void givenExistingFilesAndNewFiles_whenValidating_thenThrowsException() {
        // Given
        int existingFileCount = 3;
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "content2".getBytes()),
                new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "content3".getBytes())
        );

        // When & Then
        assertThatThrownBy(() -> sut.validateFiles(files, existingFileCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 5개의 파일만 업로드 가능합니다");
    }

    @DisplayName("파일 크기가 10MB를 초과하면, 예외를 던진다")
    @Test
    void givenOversizedFile_whenValidating_thenThrowsException() {
        // Given
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", largeContent)
        );

        // When & Then
        assertThatThrownBy(() -> sut.validateFiles(files))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일 크기는 10MB를 초과할 수 없습니다");
    }

    @DisplayName("지원하지 않는 파일 형식이면, 예외를 던진다")
    @Test
    void givenUnsupportedFileType_whenValidating_thenThrowsException() {
        // Given
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.txt", "text/plain", "content1".getBytes())
        );

        // When & Then
        assertThatThrownBy(() -> sut.validateFiles(files))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 파일 형식입니다");
    }

    @DisplayName("빈 파일 목록이 주어지면, 아무것도 하지 않는다")
    @Test
    void givenEmptyFileList_whenValidating_thenDoesNothing() {
        // Given
        List<MultipartFile> files = List.of();

        // When & Then
        assertThatCode(() -> sut.validateFiles(files))
                .doesNotThrowAnyException();
    }
}