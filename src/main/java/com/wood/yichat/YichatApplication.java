package com.wood.yichat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 启动类
 *
 */
@SpringBootApplication
@MapperScan("com.wood.yichat.mapper")
@EnableScheduling
@EnableSwagger2WebMvc
public class YichatApplication {

    public static void main(String[] args) {
        SpringApplication.run(YichatApplication.class, args);
    }

}
