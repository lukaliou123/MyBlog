package com.example.blogapi.dao.pojo;

import lombok.Data;

@Data
public class ArticleTag {

    private String id;

    private Long articleId;

    private Long tagId;
}