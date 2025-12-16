package com.whd.cube;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库配置
        String url = "jdbc:mysql://localhost:3306/cube_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "saveme";

        // 2. 开始生成
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("WHD") // 设置作者
                            .disableOpenDir() // 禁止打开输出目录
                            .outputDir(System.getProperty("user.dir") + "/src/main/java");
                })
                .packageConfig(builder -> {
                    builder.parent("com.whd") // 设置父包名
                            .moduleName("cube") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude(
                                    "sys_user", "event", "competition",
                                    "competition_event", "competition_schedule",
                                    "registration", "registration_item",
                                    "result", "article"
                            )
                            .addTablePrefix("sys_") // 设置过滤表前缀

                            // Entity 策略配置
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation() // 生成字段注解
                            .idType(IdType.AUTO) // ★★★ 重点：你的数据库是自增ID，这里必须设为AUTO，否则MP会默认用雪花算法生成ID

                            // Controller 策略配置
                            .controllerBuilder()
                            .enableRestStyle() // 开启生成@RestController

                            // Service 策略配置
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // 去掉Service接口的首字母I
                            .formatServiceImplFileName("%sServiceImpl");
                })
                // 模板引擎配置
                .templateEngine(new VelocityTemplateEngine())
                // 注入自定义模板路径 (虽然放在 resources/templates 下会自动读取，但显式指定更稳妥)
                .templateConfig(builder -> {
                    builder.entity("/templates/entity.java");
                })
                .execute();

        System.out.println("✅ 代码重新生成完毕！");
    }
}