package com.example.blogapi.service;

import com.example.blogapi.vo.CategoryVo;

public interface CategoryService {
    CategoryVo findCategoryById(Long categoryId);
}
