package com.springboard.projectboard.domain.constant;

import lombok.Getter;

public enum SearchType {
    TITLE("제목"),
    CONTENT("내용"),
    ID("유저 ID"),
    NICKNAME("닉네임"),
    HASHTAG("해시태그");

    @Getter private final String description;

    SearchType(String description) {
        this.description = description;
    }
}
