package com.example.blogapi.service;
import com.example.blogapi.vo.TagVo;

import java.util.List;

public interface TagService {

    List<TagVo> findTagsByArticleId(Long articleId);
}
