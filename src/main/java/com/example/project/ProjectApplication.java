package com.example.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ProjectApplication {

        public static void main(String[] args) throws JsonProcessingException {

            ConfigurableApplicationContext context = SpringApplication.run(ProjectApplication.class, args);
            JenkinsService jenkinsService = context.getBean(JenkinsService.class);

            String response = jenkinsService.getLatestJobBuild();

        }
    }

