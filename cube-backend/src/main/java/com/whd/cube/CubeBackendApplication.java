package com.whd.cube;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.whd.cube.mapper")
public class CubeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CubeBackendApplication.class, args);
    }

}
