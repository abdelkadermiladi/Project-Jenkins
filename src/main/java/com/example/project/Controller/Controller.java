package com.example.project.Controller;


import com.example.project.Model.JenkinsJobBuild;
import com.example.project.Service.JenkinsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class Controller {

    private final JenkinsService jenkinsService;

    @Autowired
    public Controller(JenkinsService jenkinsService) {
        this.jenkinsService = jenkinsService;
    }

    @GetMapping("/last-job-build")
    public ResponseEntity<Object> getLastJobDuration() {
        try {
            JenkinsJobBuild jobBuild = jenkinsService.getLatestJobBuild();
            if (jobBuild != null) {
                String duration = "Last Job Duration : " + jobBuild.getjobDuration() + " milliseconds";
                Map<String, String> response = new HashMap<>();
                response.put("duration", duration);
                return ResponseEntity.ok().body(response);
            } else {
                return ResponseEntity.ok().body(Collections.singletonMap("message", "No job build found."));
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error processing the Jenkins job build data."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }


    //@GetMapping("/last-job-build")
    //public String getLastJobDuration() {
    //    try {
    //        JenkinsJobBuild jobBuild = jenkinsService.getLatestJobBuild();
    //        if (jobBuild != null) {
    //            return ("Last Job Duration : "+jobBuild.getjobDuration()+" millsconds");
    //        } else {
    //            return "No job build found.";
    //        }
    //     } catch (JsonProcessingException e) {
    //        return "Error processing the Jenkins job build data.";
    //    } catch (Exception e) {
    //        return "An unexpected error occurred: " + e.getMessage();
    //    }
    //}
}




