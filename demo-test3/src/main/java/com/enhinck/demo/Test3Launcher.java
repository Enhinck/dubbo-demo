package com.enhinck.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 */
@Slf4j
@EnableDubbo
@SpringBootApplication
@MapperScan(basePackages = "com.enhinck.demo.mapper")
public class Test3Launcher {

    public static void main(String[] args) {
        if (StringUtils.isBlank(System.getProperty("DUBBO_ENDPOINT_PORT"))) {
            System.setProperty("DUBBO_ENDPOINT_PORT", "20883");
        }

        log.info("start....");
        SpringApplication.run(Test3Launcher.class, args);
        System.out.println("angel-test3 start successful!");
    }
}