package com.whd.cube.config;

import com.whd.cube.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全局配置：跨域 + 拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    // 1. 跨域配置 (保持不变)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

    // 映射本地文件目录到 Web 路径
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:///D:/graduation/uploads/");
    }

    // 2. 注册拦截器 (新增)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(   // 排除不需要登录的接口

                        // 1. 基础接口
                        "/user/login",      // 登录
                        "/user/register",   // 注册
                        "/files/**",        // 图片资源
                        "/error",           // 错误页

                        // 2. 公共查询接口
                        "/article/list",    // 公告列表
                        "/article/{id:\\d+}",    // 公告详情
                        "/event/list",      // 项目字典

                        // 3. 赛事查看相关 (允许未登录查看)
                        "/competition/list",     // 赛事列表
                        "/competition/upcoming", // 近期赛事
                        "/competition/years",    // 年份筛选
                        "/competition/slug/**",  // 通过 Slug 查看赛事详情
                        "/competition-event/**", // 查看赛事下的项目
                        "/registration/competitors/**", // 查看已报名选手名单

                        // 4. 成绩查看相关
                        "/result/rankings",      // 成绩排名
                        "/result/person/**",     // 选手个人成绩
                        "/result/list",

                        // 5. 用户信息 (用于展示主办方名称等)
                        "/user/public/**",       // 选手公开信息
                        "/user/list",            // 选手列表
                        "/user/{id:\\\\d+}",             // 详情页需要查询主办方名字 (Info.vue 调用了 getUserById)
                        "/user/{id:[0-9]+}"
                );
    }
}