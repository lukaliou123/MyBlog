package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.PageParams;

public interface ArticleService {
    /**
     * 分页查询 文章列表
     * @param pageParams
     * @return
     */
    Result listArticle(PageParams pageParams);
}
