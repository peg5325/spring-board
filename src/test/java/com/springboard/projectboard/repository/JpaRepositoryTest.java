package com.springboard.projectboard.repository;

import com.springboard.projectboard.domain.Article;
import com.springboard.projectboard.domain.Hashtag;
import com.springboard.projectboard.domain.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest
@Sql(statements = {
    "CREATE TABLE IF NOT EXISTS user_account (user_id VARCHAR(50) PRIMARY KEY, user_password VARCHAR(255), email VARCHAR(100), nickname VARCHAR(100), memo TEXT, created_at TIMESTAMP, created_by VARCHAR(100), modified_at TIMESTAMP, modified_by VARCHAR(100))",
    "CREATE TABLE IF NOT EXISTS article (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(50), title VARCHAR(255), content TEXT, created_at TIMESTAMP, created_by VARCHAR(100), modified_at TIMESTAMP, modified_by VARCHAR(100))",
    "CREATE TABLE IF NOT EXISTS hashtag (id BIGINT AUTO_INCREMENT PRIMARY KEY, hashtag_name VARCHAR(100) UNIQUE, created_at TIMESTAMP, created_by VARCHAR(100), modified_at TIMESTAMP, modified_by VARCHAR(100))",
    "CREATE TABLE IF NOT EXISTS article_hashtag (article_id BIGINT, hashtag_id BIGINT, PRIMARY KEY (article_id, hashtag_id))",
    "CREATE TABLE IF NOT EXISTS article_comment (id BIGINT AUTO_INCREMENT PRIMARY KEY, article_id BIGINT, user_id VARCHAR(50), parent_comment_id BIGINT, content TEXT, created_at TIMESTAMP, created_by VARCHAR(100), modified_at TIMESTAMP, modified_by VARCHAR(100))"
})
class JpaRepositoryTest {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;
    private final HashtagRepository hashtagRepository;

    public JpaRepositoryTest(
            @Autowired ArticleRepository articleRepository,
            @Autowired ArticleCommentRepository articleCommentRepository,
            @Autowired UserAccountRepository userAccountRepository,
            @Autowired HashtagRepository hashtagRepository
    ) {
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
        this.userAccountRepository = userAccountRepository;
        this.hashtagRepository = hashtagRepository;
    }

    @DisplayName("select 테스트")
    @Test
    void givenTestData_whenSelecting_thenWorksFine() {

        // Given

        // When
        List<Article> articles = articleRepository.findAll();

        //
        assertThat(articles)
                .isNotNull()
                .hasSize(0); // 빈 데이터베이스
    }

    @DisplayName("insert 테스트")
    @Test
    void givenTestData_whenInserting_thenWorksFine() {

        // Given
        long previousCount = articleRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("newEongyu", "pw", null, null, null));
        Article article = Article.of(userAccount, "new article", "new content");
        article.addHashtags(Set.of(Hashtag.of("spring")));

        // When
        Article savedArticle = articleRepository.save(article);

        //
        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    void givenTestData_whenUpdating_thenWorksFine() {

        // Given
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("testUser", "pw", null, null, null));
        Article article = articleRepository.save(Article.of(userAccount, "test title", "test content"));
        Hashtag updateHashtag = Hashtag.of("springboot");
        article.clearHashtags();
        article.addHashtags(Set.of(updateHashtag));

        // When
        Article savedArticle = articleRepository.saveAndFlush(article);

        // Then
        assertThat(savedArticle.getHashtags())
                .hasSize(1)
                .extracting("hashtagName", String.class)
                .containsExactly(updateHashtag.getHashtagName());
    }

    @DisplayName("delete 테스트")
    @Test
    void givenTestData_whenDeleting_thenWorksFine() {

        // Given
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("testUser", "pw", null, null, null));
        Article article = articleRepository.save(Article.of(userAccount, "test title", "test content"));
        long previousArticleCount = articleRepository.count();
        long previousArticleCommentCount = articleCommentRepository.count();
        int deletedCommentsSize = article.getArticleComments().size();

        // When
        articleRepository.delete(article);

        // Then
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - deletedCommentsSize);
    }

    @DisplayName("[Querydsl] 전체 hashtag 리스트에서 이름만 조회하기")
    @Test
    void givenNothing_whenQueryingHashtags_thenReturnsHashtagNames() {
        // Given

        // When
        List<String> hashtagNames = hashtagRepository.findAllHashtagNames();

        // Then
        assertThat(hashtagNames).hasSize(0);
    }

    @DisplayName("[Querydsl] hashtag로 페이지된 게시글 검색하기")
    @Test
    void givenHashtagNamesAndPageable_whenQueryingArticles_thenReturnsArticlePage() {
        // Given
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("testUser", "pw", null, null, null));
        Article article = Article.of(userAccount, "Test Article", "Test Content");
        article.addHashtags(Set.of(Hashtag.of("blue")));
        articleRepository.save(article);
        
        List<String> hashtagNames = List.of("blue");
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
                Sort.Order.desc("hashtags.hashtagName"),
                Sort.Order.asc("title")
        ));

        // When
        Page<Article> articlePage = articleRepository.findByHashtagNames(hashtagNames, pageable);

        // Then
        assertThat(articlePage.getContent()).hasSize(1);
        assertThat(articlePage.getContent().get(0).getTitle()).isEqualTo("Test Article");
        assertThat(articlePage.getContent().get(0).getHashtags())
                .extracting("hashtagName", String.class)
                .containsExactly("blue");
        assertThat(articlePage.getTotalElements()).isEqualTo(1);
        assertThat(articlePage.getTotalPages()).isEqualTo(1);
    }

    @EnableJpaAuditing
    @TestConfiguration
    public static class TestJpaConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("eongyu");
        }
    }

}
