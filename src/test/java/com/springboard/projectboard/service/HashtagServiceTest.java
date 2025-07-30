package com.springboard.projectboard.service;

import com.springboard.projectboard.domain.Article;
import com.springboard.projectboard.domain.Hashtag;
import com.springboard.projectboard.domain.UserAccount;
import com.springboard.projectboard.dto.ArticleDto;
import com.springboard.projectboard.dto.HashtagDto;
import com.springboard.projectboard.dto.UserAccountDto;
import com.springboard.projectboard.repository.HashtagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@DisplayName("비즈니스 로직 - 해시태그")
@ExtendWith(MockitoExtension.class)
class HashtagServiceTest {

    @InjectMocks
    private HashtagService sut;

    @Mock
    private HashtagRepository hashtagRepository;

    @DisplayName("본문을 파싱하면, 해시태그 이름들을 중복 없이 반환한다.")
    @MethodSource
    @ParameterizedTest(name = "[{index}] \"{0}\" => {1}")
    void givenContent_whenParsing_thenReturnsUniqueHashtagNames(String input, Set<String> expected) {
        // Given

        // When
        Set<String> actual = sut.parseHashtagNames(input);

        // Then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        then(hashtagRepository).shouldHaveNoInteractions();
    }

    static Stream<Arguments> givenContent_whenParsing_thenReturnsUniqueHashtagNames() {
        return Stream.of(
                arguments(null, Set.of()),
                arguments("", Set.of()),
                arguments("   ", Set.of()),
                arguments("#", Set.of()),
                arguments("  #", Set.of()),
                arguments("#   ", Set.of()),
                arguments("java", Set.of()),
                arguments("java#", Set.of()),
                arguments("ja#va", Set.of("va")),
                arguments("#java", Set.of("java")),
                arguments("#java_spring", Set.of("java_spring")),
                arguments("#java-spring", Set.of("java")),
                arguments("#_java_spring", Set.of("_java_spring")),
                arguments("#-java-spring", Set.of()),
                arguments("#_java_spring__", Set.of("_java_spring__")),
                arguments("#java#spring", Set.of("java", "spring")),
                arguments("#java #spring", Set.of("java", "spring")),
                arguments("#java  #spring", Set.of("java", "spring")),
                arguments("#java   #spring", Set.of("java", "spring")),
                arguments("#java     #spring", Set.of("java", "spring")),
                arguments("  #java     #spring ", Set.of("java", "spring")),
                arguments("   #java     #spring   ", Set.of("java", "spring")),
                arguments("#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java #spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#spring #부트", Set.of("java", "spring", "부트")),
                arguments("#java,#spring,#부트", Set.of("java", "spring", "부트")),
                arguments("#java.#spring;#부트", Set.of("java", "spring", "부트")),
                arguments("#java|#spring:#부트", Set.of("java", "spring", "부트")),
                arguments("#java #spring  #부트", Set.of("java", "spring", "부트")),
                arguments("   #java,? #spring  ...  #부트 ", Set.of("java", "spring", "부트")),
                arguments("#java#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#java#java#spring#부트", Set.of("java", "spring", "부트")),
                arguments("#java#spring#java#부트#java", Set.of("java", "spring", "부트")),
                arguments("#java#스프링 아주 긴 글~~~~~~~~~~~~~~~~~~~~~", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~~~~~~~~~~~~~~~~#java#스프링", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~#java#스프링~~~~~~~~~~~~~~~", Set.of("java", "스프링")),
                arguments("아주 긴 글~~~~~~#java~~~~~~~#스프링~~~~~~~~", Set.of("java", "스프링"))
        );
    }

    @DisplayName("해시태그 이름들을 입력하면, 저장된 해시태그 중 이름에 매칭하는 것들을 중복 없이 반환한다.")
    @Test
    void givenHashtagNames_whenFindingHashtags_thenReturnsHashtagSet() {
        // Given
        Set<String> hashtagNames = Set.of("java", "spring", "boot");

        given(hashtagRepository.findByHashtagNameIn(hashtagNames)).willReturn(List.of(
                Hashtag.of("java"),
                Hashtag.of("spring")
        ));

        // When
        Set<Hashtag> hashtags = sut.findHashtagsByNames(hashtagNames);

        // Then
        assertThat(hashtags).hasSize(2);
        then(hashtagRepository).should().findByHashtagNameIn(hashtagNames);
    }

    @DisplayName("게시글과 연결되지 않은 해시태그 ID를 주면, 해시태그를 삭제한다.")
    @Test
    void givenHashtagIdWithoutArticles_whenDeletingHashtag_thenDeletesHashtag() {
        // Given
        Long hashtagId = 1L;
        Hashtag hashtag = mock(Hashtag.class);
        ReflectionTestUtils.setField(hashtag, "id", hashtagId);
        ReflectionTestUtils.setField(hashtag, "hashtagName", "java");

        given(hashtagRepository.getReferenceById(hashtagId)).willReturn(hashtag);
        given(hashtag.getArticles()).willReturn(Collections.emptySet());

        // When
        sut.deleteHashtagWithoutArticles(hashtagId);

        // Then
        then(hashtagRepository).should().getReferenceById(hashtagId);
        then(hashtagRepository).should().delete(hashtag);
    }

    @DisplayName("게시글과 연결된 해시태그 ID를 주면, 해시태그를 삭제하지 않는다.")
    @Test
    void givenHashtagIdWithArticles_whenDeletingHashtag_thenDoesNotDeleteHashtag() {
        // Given
        Long hashtagId = 2L;
        Hashtag hashtag = mock(Hashtag.class);
        ReflectionTestUtils.setField(hashtag, "id", hashtagId);
        ReflectionTestUtils.setField(hashtag, "hashtagName", "spring");

        Set<Article> articles = Set.of(createArticle());
        given(hashtagRepository.getReferenceById(hashtagId)).willReturn(hashtag);
        given(hashtag.getArticles()).willReturn(articles);

        // When
        sut.deleteHashtagWithoutArticles(hashtagId);

        // Then
        then(hashtagRepository).should().getReferenceById(hashtagId);
        then(hashtagRepository).should(never()).delete(hashtag);
        then(hashtagRepository).shouldHaveNoMoreInteractions();
    }


    private UserAccount createUserAccount() {
        return createUserAccount("eongyu");
    }

    private UserAccount createUserAccount(String userId) {
        return UserAccount.of(
                userId,
                "password",
                "eongyu@mail.com",
                "Eongyu",
                null
        );
    }

    private Article createArticle() {
        return createArticle(1L);
    }

    private Article createArticle(Long id) {
        Article article = Article.of(
                createUserAccount(),
                "title",
                "content"
        );

        article.addHashtags(Set.of(
                createHashtag(1L, "java"),
                createHashtag(2L, "spring")
        ));

        ReflectionTestUtils.setField(article, "id", id);

        return article;
    }

    private Hashtag createHashtag(String hashtagName) {
        return createHashtag(1L, hashtagName);
    }

    private Hashtag createHashtag(Long id, String hashtagName) {
        Hashtag hashtag = Hashtag.of(hashtagName);
        ReflectionTestUtils.setField(hashtag, "id", id);

        return hashtag;
    }

    private HashtagDto createdHashtagDto() {
        return HashtagDto.of("java");
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content");
    }

    private ArticleDto createArticleDto(String title, String content) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                null,
                LocalDateTime.now(),
                "Eongyu",
                LocalDateTime.now(),
                "Eongyu"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "eongyu",
                "password",
                "eongyu@mail.com",
                "Eongyu",
                "This is memo",
                LocalDateTime.now(),
                "Eongyu",
                LocalDateTime.now(),
                "Eongyu"
        );
    }
}