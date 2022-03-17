package com.example.blogapi.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.pojo.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {

    @Async("taskExecutor")

    //期望此操作在线程池执行 不会影响原有的主线程
   public void updateArticleViewCount(ArticleMapper articleMapper, Article article){

        int viewCounts = article.getViewCounts();
        Article articleUpdate = new Article();
        articleUpdate.setViewCounts(viewCounts+1);
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        //设置一个为了在多线程的环境下 线程安全
        updateWrapper.eq(Article::getViewCounts,viewCounts);
        //updatearticle set view_count = 100 where view_count =99 and id==11
        articleMapper.update(articleUpdate,updateWrapper);
       try{
           Thread.sleep(5000);
           System.out.println("更新完成了。。。");
       }catch (InterruptedException e){
           e.printStackTrace();
       }
   }
}
