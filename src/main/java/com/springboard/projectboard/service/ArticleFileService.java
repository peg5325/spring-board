package com.springboard.projectboard.service;

import com.springboard.projectboard.domain.Article;
import com.springboard.projectboard.domain.ArticleFile;
import com.springboard.projectboard.repository.ArticleFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleFileService {

    private final ArticleFileRepository articleFileRepository;
    private final S3FileUploadService s3FileUploadService;
    private final FileValidator fileValidator;

    public void saveArticleFiles(Article article, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        int existingFileCount = getArticleFiles(article.getId()).size();
        fileValidator.validateFiles(files, existingFileCount);

        int nextDisplayOrder = existingFileCount + 1;
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                saveArticleFile(article, file, nextDisplayOrder++);
            }
        }
    }

    private void saveArticleFile(Article article, MultipartFile file, int displayOrder) {
        String s3Url = s3FileUploadService.uploadFile(file, article.getId(), displayOrder);
        String s3Key = extractS3Key(s3Url);

        ArticleFile articleFile = ArticleFile.of(
                article,
                file.getOriginalFilename(),
                s3Key,
                s3Url,
                file.getSize(),
                displayOrder
        );

        articleFileRepository.save(articleFile);
    }

    @Transactional(readOnly = true)
    public List<ArticleFile> getArticleFiles(Long articleId) {
        return articleFileRepository.findByArticleIdOrderByDisplayOrderAsc(articleId);
    }

    @Transactional(readOnly = true)
    public ArticleFile getArticleFile(Long fileId) {
        return articleFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다: " + fileId));
    }

    public InputStream downloadFile(String s3Key) {
        return s3FileUploadService.downloadFile(s3Key);
    }

    public void deleteArticleFile(Long fileId) {
        ArticleFile articleFile = getArticleFile(fileId);
        s3FileUploadService.deleteFile(articleFile.getS3Key());
        articleFileRepository.delete(articleFile);
    }

    public void deleteArticleFiles(Long articleId) {
        List<ArticleFile> files = articleFileRepository.findByArticleIdOrderByDisplayOrderAsc(articleId);
        for (ArticleFile file : files) {
            s3FileUploadService.deleteFile(file.getS3Key());
        }
        articleFileRepository.deleteAll(files);
    }

    private String extractS3Key(String s3Url) {
        // S3 URL 형태: https://spring-board-image-files.s3.ap-northeast-2.amazonaws.com/articles/1/1_uuid.jpg
        try {
            if (s3Url.contains(".amazonaws.com/")) {
                String s3Key = s3Url.substring(s3Url.indexOf(".amazonaws.com/") + 15);
                log.debug("S3 URL: {}, Extracted Key: {}", s3Url, s3Key);
                return s3Key;
            }
            log.warn("Unexpected S3 URL format: {}", s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("Failed to extract S3 key from URL: {}", s3Url, e);
            return s3Url;
        }
    }
}