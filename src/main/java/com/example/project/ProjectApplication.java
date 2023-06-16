package com.example.project;

import com.example.project.Model.JenkinsJobBuild;
import com.example.project.Service.JenkinsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class ProjectApplication {

    @Autowired
    private JenkinsService jenkinsService;

    public static void main(String[] args) throws JsonProcessingException {
        ConfigurableApplicationContext context = SpringApplication.run(ProjectApplication.class, args);


        ProjectApplication application = context.getBean(ProjectApplication.class);
        application.run();
    }

    public void run() throws JsonProcessingException {

        JenkinsJobBuild response = jenkinsService.getLatestJobBuild();
        System.out.println("\n");
        System.out.println("Job name: " + response.getJobName());
        System.out.println("Build number: " + response.getBuildNumber());
        System.out.println("dateTime: " + response.getdateTime());
        System.out.println("jobDuration: " + response.getjobDuration() + " milliseconds");

    }
}
