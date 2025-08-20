package com.springboard.projectboard.controller;

import com.springboard.projectboard.domain.ArticleFile;
import com.springboard.projectboard.service.ArticleFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.springboard.projectboard.dto.security.BoardPrincipal;


import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
public class FileController {

    private final ArticleFileService articleFileService;

    @GetMapping("/download/{fileId}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long fileId) {
        ArticleFile articleFile = articleFileService.getArticleFile(fileId);
        InputStream inputStream = articleFileService.downloadFile(articleFile.getS3Key());
        
        String encodedFilename = URLEncoder.encode(articleFile.getOriginalFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        if (boardPrincipal == null) {
            return ResponseEntity.status(403).build();
        }
        
        // 파일 소유권 확인
        ArticleFile articleFile = articleFileService.getArticleFile(fileId);
        if (!articleFile.getArticle().getUserAccount().getUserId().equals(boardPrincipal.getUsername())) {
            return ResponseEntity.status(403).build();
        }
        
        articleFileService.deleteArticleFile(fileId);
        return ResponseEntity.ok().build();
    }
}