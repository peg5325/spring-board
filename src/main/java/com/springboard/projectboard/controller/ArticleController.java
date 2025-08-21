package com.springboard.projectboard.controller;

import com.springboard.projectboard.domain.Article;
import com.springboard.projectboard.domain.constant.FormStatus;
import com.springboard.projectboard.domain.constant.SearchType;
import com.springboard.projectboard.dto.request.ArticleRequest;
import com.springboard.projectboard.dto.response.ArticleResponse;
import com.springboard.projectboard.dto.response.ArticleWithCommentsResponse;
import com.springboard.projectboard.dto.security.BoardPrincipal;
import com.springboard.projectboard.service.ArticleService;
import com.springboard.projectboard.service.ArticleFileService;
import com.springboard.projectboard.service.PaginationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/articles")
@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleFileService articleFileService;
    private final PaginationService paginationService;

    @GetMapping
    public String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchValue, pageable)
                .map(dto -> {
                    boolean hasFiles = !articleFileService.getArticleFiles(dto.id()).isEmpty();
                    return ArticleResponse.of(
                            dto.id(),
                            dto.title(),
                            dto.content(),
                            dto.hashtagDtos().stream().map(hashtagDto -> hashtagDto.hashtagName()).collect(java.util.stream.Collectors.toSet()),
                            dto.createdAt(),
                            dto.userAccountDto().email(),
                            dto.userAccountDto().nickname() != null && !dto.userAccountDto().nickname().isBlank() ? dto.userAccountDto().nickname() : dto.userAccountDto().userId(),
                            hasFiles
                    );
                });
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        map.addAttribute("articles", articles);
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchTypes", SearchType.values());
        map.addAttribute("searchTypeHashtag", SearchType.HASHTAG);

        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public String article(@PathVariable Long articleId, ModelMap map) {
        ArticleWithCommentsResponse article = ArticleWithCommentsResponse.from(articleService.getArticleWithComments(articleId));

        map.addAttribute("article", article);
        map.addAttribute("articleComments", article.articleCommentResponse());
        map.addAttribute("articleFiles", articleFileService.getArticleFiles(articleId));
        map.addAttribute("previousArticleId", articleService.getPreviousArticleId(articleId));
        map.addAttribute("nextArticleId", articleService.getNextArticleId(articleId));
        map.addAttribute("searchTypeHashtag", SearchType.HASHTAG);

        return "articles/detail";
    }

    @GetMapping("/search-hashtag")
    public String searchArticleHashtag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<ArticleResponse> articles = articleService.searchArticlesViaHashtag(searchValue, pageable)
                .map(dto -> {
                    boolean hasFiles = !articleFileService.getArticleFiles(dto.id()).isEmpty();
                    return ArticleResponse.of(
                            dto.id(),
                            dto.title(),
                            dto.content(),
                            dto.hashtagDtos().stream().map(hashtagDto -> hashtagDto.hashtagName()).collect(java.util.stream.Collectors.toSet()),
                            dto.createdAt(),
                            dto.userAccountDto().email(),
                            dto.userAccountDto().nickname() != null && !dto.userAccountDto().nickname().isBlank() ? dto.userAccountDto().nickname() : dto.userAccountDto().userId(),
                            hasFiles
                    );
                });
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());
        List<String> hashtags = articleService.getHashtags();

        map.addAttribute("articles", articles);
        map.addAttribute("hashtags", hashtags);
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchType", SearchType.HASHTAG);

        return "articles/search-hashtag";
    }

    @GetMapping("/form")
    public String articleForm(ModelMap map) {
        map.addAttribute("formStatus", FormStatus.CREATE);

        return "articles/form";
    }

    @PostMapping("/form")
    public String postNewArticle(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            ArticleRequest articleRequest,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        if (files != null && !files.isEmpty() && files.stream().anyMatch(file -> !file.isEmpty())) {
            articleService.saveArticleWithFiles(articleRequest.toDto(boardPrincipal.toDto()), files);
        } else {
            articleService.saveArticle(articleRequest.toDto(boardPrincipal.toDto()));
        }

        return "redirect:/articles";
    }

    @GetMapping("/{articleId}/form")
    public String updateArticleForm(@PathVariable Long articleId, ModelMap map) {
        ArticleResponse article = ArticleResponse.from(articleService.getArticle(articleId));

        map.addAttribute("article", article);
        map.addAttribute("articleFiles", articleFileService.getArticleFiles(articleId));
        map.addAttribute("formStatus", FormStatus.UPDATE);

        return "articles/form";
    }

    @PostMapping("/{articleId}/form")
    public String updateArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            ArticleRequest articleRequest,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        articleService.updateArticle(articleId, articleRequest.toDto(boardPrincipal.toDto()));
        
        if (files != null && !files.isEmpty() && files.stream().anyMatch(file -> !file.isEmpty())) {
            Article article = articleService.getArticleEntity(articleId);
            articleFileService.saveArticleFiles(article, files);
        }

        return "redirect:/articles/" + articleId;
    }

    @PostMapping("/{articleId}/delete")
    public String deleteArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
            ) {
        articleService.deleteArticle(articleId, boardPrincipal.getUsername());

        return "redirect:/articles";
    }
}
