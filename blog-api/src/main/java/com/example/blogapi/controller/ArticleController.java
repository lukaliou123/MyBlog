package com.example.blogapi.controller;

import com.example.blogapi.service.ArticleService;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//JSO数据进行交互，是@ResponseBody和@Controller的组合注解
@RestController
@RequestMapping("articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    /**
     * 首页 文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    public Result listArticle(@RequestBody PageParams pageParams){
        return articleService.listArticle(pageParams);
    }

    /**
     * 首页 最热文章
     * @return
     */
    @PostMapping("hot")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);
    }

    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("new")
    public Result newArticles(){
        int limit = 5;
        return articleService.newArticles(limit);
    }

    /**
     * 首页 最新文章
     * @return
     */
    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId);
    }

    //接口irl:/articles/publish
    //
    //请求方式：POST
    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

}
