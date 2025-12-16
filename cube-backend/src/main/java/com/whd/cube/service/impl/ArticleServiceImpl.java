package com.whd.cube.service.impl;

import com.whd.cube.entity.Article;
import com.whd.cube.mapper.ArticleMapper;
import com.whd.cube.service.ArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 新闻公告表 服务实现类
 * </p>
 *
 * @author WHD
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

}
