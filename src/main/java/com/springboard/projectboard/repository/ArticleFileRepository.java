package com.springboard.projectboard.repository;

import com.springboard.projectboard.domain.ArticleFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleFileRepository extends JpaRepository<ArticleFile, Long> {
    List<ArticleFile> findByArticleIdOrderByDisplayOrderAsc(Long articleId);
}