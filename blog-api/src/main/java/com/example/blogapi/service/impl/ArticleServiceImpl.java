package com.example.blogapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.dos.Archives;
import com.example.blogapi.dao.mapper.ArticleBodyMapper;
import com.example.blogapi.dao.mapper.ArticleMapper;
import com.example.blogapi.dao.mapper.ArticleTagMapper;
import com.example.blogapi.dao.pojo.Article;
import com.example.blogapi.dao.pojo.ArticleBody;
import com.example.blogapi.dao.pojo.ArticleTag;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.*;
import com.example.blogapi.util.UserThreadLocal;
import com.example.blogapi.vo.*;
import com.example.blogapi.vo.params.ArticleParam;
import com.example.blogapi.vo.params.PageParams;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagService tagservice;
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public Result listArticle(PageParams pageParams){
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<Article> articleIpage= articleMapper.listArticle(page
                ,pageParams.getCategoryId()
                , pageParams.getTagId()
                , pageParams.getYear()
                ,pageParams.getMonth());
        List<Article> records = articleIpage.getRecords();
        return Result.success(copyList(records,true,true));
    }
//    /**
//     * 1.分页查询article数据库表
//     * @param pageParams
//     * @return
//     */
//    @Override
//    public Result listArticle(PageParams pageParams) {
//        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        if(pageParams.getCategoryId()!=null){
//            // and category_id=#(categoryId)
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList = new ArrayList<>();
//        if(pageParams.getTagId() !=null){
//            //加入标签 条件查询
//            //atricle表中 并没有tag字段 一篇文章有多个标签
//            //article_tag article_id 1:n tag_id
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for(ArticleTag articleTag : articleTags){
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if(articleIdList.size()>0){
//                //and id in(1,2,3)
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//            //articleTagMapper.selectList()
//        }
//        //是否置顶进行排序
//        //order by create_date desc
//        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
//        Page<Article> articlePage = articleMapper.selectPage(page,queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //能直接返回吗，不能
//        List<ArticleVo> articleVoList = copyList(records,true,true);
//        return Result.success(articleVoList);
//    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //这里根据浏览量进行倒叙
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by view_counts desc limit
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by create_date desc limit
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;
    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 1.根据ID查询 文章信息
         * 2.根据bodyId和categoryId 去做关联查询
         */

        Article article = this.articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article,true,true,true,true);
        //查看完文章了，新增阅读数，有没有问题呢
        //查看完文章之后，本应该直接返回数据了，这时候做了一个更新，更新时加写锁，阻塞其他的读操作，性能会比较低
        //更新 增加了此次接口的 耗时 如果一旦更新出问题，不能影响 查看文章的操作
        //线程池 可以把更新操作 扔到线程池中执行，和主线程就互不相关了
        threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }


    @Override
    public Result publish(ArticleParam articleParam) {
        //此接口 要加入到登录拦截当中
        SysUser sysUser = UserThreadLocal.get();
        /**
         * 1.发布文章 目的 构建Article对象
         * 2.作者id 当前的登录用户
         * 3.标签 要将标签加入到关联列表当中
         * 4.body 内容存储 article bodyId
         */
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        //插入之后 会生成一个文章id
        this.articleMapper.insert(article);
        //tag
        List<TagVo> tags = articleParam.getTags();
        if(tags!=null){
            for(TagVo tag : tags){
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTag.setArticleId((articleId));
                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);

        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String,String> map = new HashMap<>();
        map.put("id",article.getId().toString());
            //发送一条消息给rocketmq 当前文章更新了，更新一下缓存吧
        ArticleMessage articleMessage = new ArticleMessage();
        articleMessage.setArticleId(article.getId());
        rocketMQTemplate.convertAndSend("blog-update-article",articleMessage);
        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records,Boolean isTag, Boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for(Article record : records){
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for(Article record : records){
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article,boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory){
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
        BeanUtils.copyProperties(article,articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:MM"));
        //并不是所有的接口都需要标签，作者信息
        if(isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagservice.findTagsByArticleId(articleId));
        }if(isAuthor){
            //查作者ID
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if(isBody){
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }

        if(isCategory){
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }
        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }


}
