package com.example.project.Controller;
import com.example.project.Model.AuthHeaders;
import com.example.project.Model.JenkinsJobBuild;
import com.example.project.Service.JenkinsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController

@RequestMapping("/api")

public class FinalController {

    private final JenkinsService jenkinsService;
    private AuthHeaders authHeaders;

    @Autowired
    public FinalController(JenkinsService jenkinsServ) {
        System.out.println("start FinalController()");

        this.jenkinsService = jenkinsServ;
        this.authHeaders = new AuthHeaders();
    }


    //////////////////////////////////////////////////////////////////
    @GetMapping("/last-job-build-description")
    public ResponseEntity<Object> getLastJobDescription() {
        System.out.println("start getLastJobDescription()");
        try {
            HttpHeaders headers = authHeaders.getHeaders(); // Retrieve the authentication headers from the session-scoped bean

            if (headers == null) {
                System.out.println("headers null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Not authenticated."));
            }
            else {
                System.out.println("headers =" + headers.toString());
            }
            JenkinsJobBuild jobBuild = jenkinsService.getLatestJobBuild(headers);
            if (jobBuild != null) {
                String jobname = jobBuild.getJobName();
                int buildnumber = jobBuild.getBuildNumber();
                LocalDateTime date = jobBuild.getdateTime();
                String duration = jobBuild.getjobDuration() + " millisecondes";
                String jobStatus = jobBuild.getJobStatus();
                Map<String, String> response = new HashMap<>();
                response.put("jobname", jobname);
                response.put("buildnumber", String.valueOf(buildnumber));
                response.put("date", String.valueOf(date));
                response.put("duration", duration);
                response.put("jobStatus", jobStatus);

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


    //////////////////////////////////////////////////////////////////
    @GetMapping("/job-builds-last-hour")
    public ResponseEntity<Object> getJobBuildsLastHour() {
        try {

            HttpHeaders headers = authHeaders.getHeaders(); // Retrieve the authentication headers from the session-scoped bean
            if (headers == null) {
                System.out.println("headers null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Not authenticated."));
            }
            else {
                System.out.println("headers =" + headers.toString());
            }

            // Define the start and end time for the time range
            LocalDateTime startTime = LocalDateTime.now().minusHours(1); // Example: 1 hours ago
            LocalDateTime endTime = LocalDateTime.now(); // Example: current time

            // Get all job names
            List<String> jobNames = jenkinsService.getAllJobNames(headers);

            List<Map<String, String>> response = new ArrayList<>();

            for (String jobName : jobNames) {
                // Get job builds within the specified time range for each job name
                List<JenkinsJobBuild> jobBuildsInRange = jenkinsService.getJobBuildsByTimeRange1(headers,startTime, endTime, jobName);

                for (JenkinsJobBuild jobBuild : jobBuildsInRange) {

                    Map<String, String> jobBuildData = new HashMap<>();
                    jobBuildData.put("jobname", jobBuild.getJobName());
                    jobBuildData.put("buildnumber", String.valueOf(jobBuild.getBuildNumber()));
                    jobBuildData.put("Start date", String.valueOf(jobBuild.getdateTime()));
                    jobBuildData.put("duration", jobBuild.getjobDuration() + " milliseconds");

                    response.add(jobBuildData);
                }
            }

            if (!response.isEmpty()) {
                return ResponseEntity.ok().body(response);
            } else {
                return ResponseEntity.ok().body(Collections.singletonMap("message", "No job builds found within the specified time range."));
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error processing the Jenkins job build data."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    //////////////////////////////////////////////////////////////////
    @GetMapping("/nodeNames")
    public ResponseEntity<List<String>> getNodeNames() {
        try {
            HttpHeaders headers = authHeaders.getHeaders(); // Retrieve the authentication headers from the session-scoped bean

            List<String> nodeNames = jenkinsService.getNodesNames(headers);
            return ResponseEntity.ok(nodeNames);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //////////////////////////////////////////////////////////////////

    @GetMapping("/get-job-info")
    public ResponseEntity<Object> getallJobInfo() {

        HttpHeaders headers = authHeaders.getHeaders(); // Retrieve the authentication headers from the session-scoped bean

        return jenkinsService.getallJobInfo(headers);
    }

    //////////////////////////////////////////////////////////////////
    @PostMapping("/job-builds-by-time-range-picker")
    public ResponseEntity<Object> getJobBuildsByTimeRangePicker(@RequestBody Map<String, String> dateData) {
        try {

            HttpHeaders headers = authHeaders.getHeaders(); // Retrieve the authentication headers from the session-scoped bean

            if (headers == null) {
                System.out.println("headers null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Not authenticated."));
            }
            else {
                System.out.println("headers =" + headers.toString());
            }


            // Get job names from the Jenkins server
            List<String> jobNames = jenkinsService.getAllJobNames(headers);


            String startTime = dateData.get("startTime");
            String endTime = dateData.get("endTime");
            String selectedNode = dateData.get("selectedNode");



            // Prepare the response list to store data for each job build
            List<Map<String, String>> response = new ArrayList<>();

            // Loop through the job names and get job builds within the specified time range for each job
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startTimeD = LocalDateTime.parse(startTime, formatter);
            LocalDateTime endTimeD = LocalDateTime.parse(endTime, formatter);

            ResponseEntity<Object> AllJobInfoResponse = jenkinsService.getallJobInfo(headers);
            Object AllJobInfoObject = AllJobInfoResponse.getBody();

            if (AllJobInfoObject instanceof List) {
                List<Map<String, String>> AllJobInfo = (List<Map<String, String>>) AllJobInfoObject;

                for (String jobName : jobNames) {
                    List<JenkinsJobBuild> jobBuildsInRange = jenkinsService.getJobBuildsByTimeRange2(headers,startTimeD, endTimeD, jobName);

                    for (JenkinsJobBuild jobBuild : jobBuildsInRange) {
                        // The object to find
                        Map<String, String> objectToFind = new HashMap<>();
                        objectToFind.put("number", String.valueOf(jobBuild.getBuildNumber()));
                        objectToFind.put("name", jobBuild.getJobName());
                        //The Selected Node Here :
                        //objectToFind.put("builtOn", "Madrid");
                        if (selectedNode.equals("maître"))
                            {objectToFind.put("builtOn", "");}
                        else
                            {objectToFind.put("builtOn", selectedNode);}

                        // Loop through the 'AllJobInfo' list and check if the object is present
                        for (Map<String, String> jobInfo : AllJobInfo) {
                            if (jobInfo.equals(objectToFind)) {
                                Map<String, String> jobBuildData = new HashMap<>();
                                jobBuildData.put("jobname", jobBuild.getJobName());
                                jobBuildData.put("buildnumber", String.valueOf(jobBuild.getBuildNumber()));
                                jobBuildData.put("date", String.valueOf(jobBuild.getdateTime()));
                                jobBuildData.put("duration", jobBuild.getjobDuration() + " milliseconds");
                                jobBuildData.put("queuingDuration", jobBuild.getQueuingDuration() + " milliseconds");
                                jobBuildData.put("jobStatus", jobBuild.getJobStatus());
                                jobBuildData.put("TheEndTime", String.valueOf(jobBuild.getTheEndTime()));
                                jobBuildData.put("ExecutionDate", String.valueOf(jobBuild.getExecutionDate()));

                                response.add(jobBuildData);
                                break; // If we found the object, no need to continue searching
                            }
                        }
                    }
                }

                if (!response.isEmpty()) {
                    return ResponseEntity.ok().body(response);
                } else {
                    return ResponseEntity.ok().body(Collections.singletonMap("message", "No job builds found within the specified time range."));
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Unexpected response format from Jenkins."));
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error processing the Jenkins job build data."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    //////////////////////////////////////////////////////////////////

    @PostMapping("/SelectedNode")
    public ResponseEntity<Object> getSelectedNode(@RequestBody Map<String, String> requestData) {
        try {
            String selectedNode = requestData.get("selectedNode");
            // Your logic to process the selectedNode here

            // For example, you can return a response with the selectedNode
            Map<String, String> response = new HashMap<>();
            response.put("selectedNode", selectedNode);

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            // Handle any exceptions that occur during processing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    //////////////////////////////////////////////////////////////////

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Authenticate the user using JenkinsService
        HttpHeaders headers = jenkinsService.getAuthHeadersJenkins(username, password);

        if (headers != null) {
            // If authentication is successful, set the headers in the session-scoped bean
            authHeaders.setHeaders(headers);
            HttpHeaders s = authHeaders.getHeaders();
            System.out.println("authenticate:" + s.toString());

            return ResponseEntity.ok().build();
        } else {
            // If authentication fails, return a 401 Unauthorized response
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}

