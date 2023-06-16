package com.example.project.Controller;


import com.example.project.Model.JenkinsJobBuild;
import com.example.project.Service.JenkinsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")

public class Controller {

    private final JenkinsService jenkinsService;

    @Autowired
    public Controller(JenkinsService jenkinsService) {
        this.jenkinsService = jenkinsService;
    }
    @GetMapping("/last-job-build")
    public String getLastJobName() {
        try {
            JenkinsJobBuild jobBuild = jenkinsService.getLatestJobBuild();
            if (jobBuild != null) {
                return jobBuild.getJobName();
            } else {
                return "No job build found.";
            }
        } catch (JsonProcessingException e) {
            return "Error processing the Jenkins job build data.";
        } catch (Exception e) {
            return "An unexpected error occurred: " + e.getMessage();
        }
    }
}




