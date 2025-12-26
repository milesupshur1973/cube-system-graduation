package com.whd.cube.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext;
import com.whd.cube.entity.Article;
import com.whd.cube.entity.User;
import com.whd.cube.service.ArticleService;
import com.whd.cube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 新闻公告表 前端控制器
 * </p>
 *
 * @author WHD
 */
@RestController
@RequestMapping("/article")
@CrossOrigin
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "5") Integer size) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("publish_time");
        // 使用 MyBatis Plus 分页查询
        Page<Article> articlePage = new Page<>(page, size);
        Page<Article> resultPage = articleService.page(articlePage, wrapper);
        return Result.success(resultPage);
    }

    // 补回详情接口 (阅读全文用)
    @GetMapping("/{id}")
    public Result getDetail(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    // 发布公告接口
    @PostMapping("/publish")
    public Result publish(@RequestBody Article article) {
        // 1. 获取当前登录用户 ID
        Long currentUserId = UserContext.getUserId();

        // 2. 查询用户信息，判断角色
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error("权限不足：只有管理员可以发布公告");
        }

        // 3. 校验参数
        if (article.getTitle() == null || article.getContent() == null) {
            return Result.error("标题和内容不能为空");
        }

        // 4. 填充默认值
        article.setUserId(currentUserId);
        article.setPublishTime(LocalDateTime.now());

        if (article.getCategory() == null) {
            article.setCategory("news");
        }

        // 5. 保存
        articleService.save(article);
        return Result.success("发布成功");
    }
}