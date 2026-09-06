package com.projectmatch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.projectmatch.mapper")
public class ProjectMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectMatchApplication.class, args);
    }
}
