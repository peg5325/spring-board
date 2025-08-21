package com.springboard.projectboard.service;

import com.springboard.projectboard.domain.Article;
import com.springboard.projectboard.domain.ArticleFile;
import com.springboard.projectboard.domain.UserAccount;
import com.springboard.projectboard.repository.ArticleFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 게시글 파일")
@ExtendWith(MockitoExtension.class)
class ArticleFileServiceTest {

    @InjectMocks private ArticleFileService sut;

    @Mock private ArticleFileRepository articleFileRepository;
    @Mock private S3FileUploadService s3FileUploadService;
    @Mock private FileValidator fileValidator;

    @DisplayName("게시글과 파일 목록이 주어지면, 파일들을 저장한다")
    @Test
    void givenArticleAndFiles_whenSavingFiles_thenSavesFiles() {
        // Given
        Article article = createArticle();
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "content2".getBytes())
        );

        given(articleFileRepository.findByArticleIdOrderByDisplayOrderAsc(article.getId())).willReturn(List.of());
        willDoNothing().given(fileValidator).validateFiles(any(), anyInt());
        given(s3FileUploadService.uploadFile(any(), any(), anyInt())).willReturn("https://test-url.com/file");

        // When
        sut.saveArticleFiles(article, files);

        // Then
        then(fileValidator).should().validateFiles(files, 0);
        then(s3FileUploadService).should(times(2)).uploadFile(any(), eq(article.getId()), anyInt());
        then(articleFileRepository).should(times(2)).save(any(ArticleFile.class));
    }

    @DisplayName("파일 ID가 주어지면, 파일 정보를 반환한다")
    @Test
    void givenFileId_whenGettingFile_thenReturnsFile() {
        // Given
        Long fileId = 1L;
        ArticleFile expectedFile = createArticleFile();

        given(articleFileRepository.findById(fileId)).willReturn(Optional.of(expectedFile));

        // When
        ArticleFile actualFile = sut.getArticleFile(fileId);

        // Then
        assertThat(actualFile).isEqualTo(expectedFile);
        then(articleFileRepository).should().findById(fileId);
    }

    @DisplayName("존재하지 않는 파일 ID가 주어지면, 예외를 던진다")
    @Test
    void givenNonexistentFileId_whenGettingFile_thenThrowsException() {
        // Given
        Long fileId = 1L;

        given(articleFileRepository.findById(fileId)).willReturn(Optional.empty());

        // When
        Throwable t = catchThrowable(() -> sut.getArticleFile(fileId));

        // Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("파일을 찾을 수 없습니다: " + fileId);
    }

    @DisplayName("S3 키가 주어지면, 파일 스트림을 반환한다")
    @Test
    void givenS3Key_whenDownloadingFile_thenReturnsInputStream() {
        // Given
        String s3Key = "articles/1/1_uuid.jpg";
        ByteArrayInputStream expectedStream = new ByteArrayInputStream("content".getBytes());

        given(s3FileUploadService.downloadFile(s3Key)).willReturn(expectedStream);

        // When
        var actualStream = sut.downloadFile(s3Key);

        // Then
        assertThat(actualStream).isEqualTo(expectedStream);
        then(s3FileUploadService).should().downloadFile(s3Key);
    }

    @DisplayName("파일 ID가 주어지면, 파일을 삭제한다")
    @Test
    void givenFileId_whenDeletingFile_thenDeletesFile() {
        // Given
        Long fileId = 1L;
        ArticleFile articleFile = createArticleFile();

        given(articleFileRepository.findById(fileId)).willReturn(Optional.of(articleFile));
        willDoNothing().given(s3FileUploadService).deleteFile(any());
        willDoNothing().given(articleFileRepository).delete(any());

        // When
        sut.deleteArticleFile(fileId);

        // Then
        then(s3FileUploadService).should().deleteFile(articleFile.getS3Key());
        then(articleFileRepository).should().delete(articleFile);
    }

    @DisplayName("게시글 ID가 주어지면, 해당 게시글의 모든 파일을 삭제한다")
    @Test
    void givenArticleId_whenDeletingArticleFiles_thenDeletesAllFiles() {
        // Given
        Long articleId = 1L;
        List<ArticleFile> files = List.of(createArticleFile(), createArticleFile());

        given(articleFileRepository.findByArticleIdOrderByDisplayOrderAsc(articleId)).willReturn(files);
        willDoNothing().given(s3FileUploadService).deleteFile(any());
        willDoNothing().given(articleFileRepository).deleteAll(any());

        // When
        sut.deleteArticleFiles(articleId);

        // Then
        then(s3FileUploadService).should(times(2)).deleteFile(any());
        then(articleFileRepository).should().deleteAll(files);
    }

    private Article createArticle() {
        Article article = Article.of(createUserAccount(), "title", "content");
        ReflectionTestUtils.setField(article, "id", 1L);
        return article;
    }

    private UserAccount createUserAccount() {
        return UserAccount.of("eongyu", "pw", "test@email.com", "Eongyu", "memo");
    }

    private ArticleFile createArticleFile() {
        return ArticleFile.of(
                createArticle(),
                "test.jpg",
                "articles/1/1_uuid.jpg",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/articles/1/1_uuid.jpg",
                1024L,
                1
        );
    }
}