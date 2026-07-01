package com.echostudy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.echostudy.mapper")
@SpringBootApplication
public class EchoStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EchoStudyApplication.class, args);
    }
}
