package com.zjb.mjgl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.zjb.mjgl.mapper")
@SpringBootApplication
@EnableScheduling
public class MjglApplication {

    public static void main(String[] args) {
        SpringApplication.run(MjglApplication.class, args);
    }

}
