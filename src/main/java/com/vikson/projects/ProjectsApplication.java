package com.vikson.projects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.vikson"})
@EnableAsync
@EnableCaching
@EnableScheduling
public class ProjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectsApplication.class, args);
    }
}
