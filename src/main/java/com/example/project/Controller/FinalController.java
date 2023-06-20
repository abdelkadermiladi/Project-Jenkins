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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController

@RequestMapping("/api")

public class FinalController {

    private final JenkinsService jenkinsService;

    @Autowired
    public FinalController(JenkinsService jenkinsService) {
        this.jenkinsService = jenkinsService;
    }

    @GetMapping("/last-job-build-description")
    public ResponseEntity<Object> getLastJobDescription() {
        try {
            JenkinsJobBuild jobBuild = jenkinsService.getLatestJobBuild();
            if (jobBuild != null) {
                String jobname = jobBuild.getJobName() ;
                int buildnumber = jobBuild.getBuildNumber() ;
                LocalDateTime date = jobBuild.getdateTime();
                String duration = jobBuild.getjobDuration() +" millisecondes" ;
                Map<String, String> response = new HashMap<>();
                response.put("jobname", jobname);
                response.put("buildnumber", String.valueOf(buildnumber));
                response.put("date", String.valueOf(date));
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


}




