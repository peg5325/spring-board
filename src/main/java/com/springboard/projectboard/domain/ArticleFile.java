package com.springboard.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Objects;

@Getter
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList = "articleId"),
        @Index(columnList = "displayOrder")
})
@Entity
public class ArticleFile extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "articleId")
    private Article article;

    @Setter
    @Column(nullable = false)
    private String originalFileName;

    @Setter
    @Column(nullable = false)
    private String s3Key;

    @Setter
    @Column(nullable = false)
    private String s3Url;

    @Setter
    @Column(nullable = false)
    private Long fileSize;

    @Setter
    private Integer displayOrder;

    protected ArticleFile() {}

    private ArticleFile(Article article, String originalFileName, String s3Key, String s3Url, Long fileSize, Integer displayOrder) {
        this.article = article;
        this.originalFileName = originalFileName;
        this.s3Key = s3Key;
        this.s3Url = s3Url;
        this.fileSize = fileSize;
        this.displayOrder = displayOrder;
    }

    public static ArticleFile of(Article article, String originalFileName, String s3Key, String s3Url, Long fileSize, Integer displayOrder) {
        return new ArticleFile(article, originalFileName, s3Key, s3Url, fileSize, displayOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleFile that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}