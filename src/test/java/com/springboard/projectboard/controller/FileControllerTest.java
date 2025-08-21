package com.springboard.projectboard.controller;

import com.springboard.projectboard.config.TestSecurityConfig;
import com.springboard.projectboard.domain.ArticleFile;
import com.springboard.projectboard.service.ArticleFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 파일")
@Import(TestSecurityConfig.class)
@WebMvcTest(FileController.class)
class FileControllerTest {

    private final MockMvc mvc;

    @MockBean private ArticleFileService articleFileService;

    public FileControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[view][GET] 파일 다운로드 - 정상 호출")
    @Test
    void givenFileId_whenRequestingFileDownload_thenReturnsFile() throws Exception {
        // Given
        Long fileId = 1L;
        ArticleFile articleFile = createArticleFile();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        given(articleFileService.getArticleFile(fileId)).willReturn(articleFile);
        given(articleFileService.downloadFile(any())).willReturn(inputStream);

        // When & Then
        mvc.perform(get("/files/download/" + fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename*=UTF-8''test.jpg"));

        then(articleFileService).should().getArticleFile(fileId);
        then(articleFileService).should().downloadFile(any());
    }

    @WithUserDetails(value = "eongyuTest", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][DELETE] 파일 삭제 - 정상 호출")
    @Test
    void givenFileId_whenRequestingFileDeletion_thenDeletesFile() throws Exception {
        // Given
        Long fileId = 1L;
        ArticleFile articleFile = createArticleFileWithUser("eongyuTest");

        given(articleFileService.getArticleFile(fileId)).willReturn(articleFile);
        willDoNothing().given(articleFileService).deleteArticleFile(fileId);

        // When & Then
        mvc.perform(
                delete("/files/" + fileId)
                        .contentType("application/json")
                        .with(csrf())
                )
                .andExpect(status().isOk());

        then(articleFileService).should().getArticleFile(fileId);
        then(articleFileService).should().deleteArticleFile(fileId);
    }

    @DisplayName("[view][DELETE] 파일 삭제 - 인증 없을 때 리다이렉트")
    @Test
    void givenFileIdWithoutAuth_whenRequestingFileDeletion_thenRedirectsToLogin() throws Exception {
        // Given
        Long fileId = 1L;

        // When & Then
        mvc.perform(
                delete("/files/" + fileId)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        then(articleFileService).shouldHaveNoInteractions();
    }

    private ArticleFile createArticleFile() {
        return ArticleFile.of(
                null,
                "test.jpg",
                "articles/1/1_uuid.jpg",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/articles/1/1_uuid.jpg",
                1024L,
                1
        );
    }

    private ArticleFile createArticleFileWithUser(String userId) {
        return ArticleFile.of(
                createArticleWithUser(userId),
                "test.jpg",
                "articles/1/1_uuid.jpg",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/articles/1/1_uuid.jpg",
                1024L,
                1
        );
    }

    private com.springboard.projectboard.domain.Article createArticleWithUser(String userId) {
        com.springboard.projectboard.domain.UserAccount userAccount = 
                com.springboard.projectboard.domain.UserAccount.of(userId, "pw", "test@email.com", "Test User", "memo");
        return com.springboard.projectboard.domain.Article.of(userAccount, "title", "content");
    }
}