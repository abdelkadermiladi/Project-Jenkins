package com.example.project.Service;
import com.example.project.Model.JenkinsJobBuild;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class JenkinsService {

    @Value("${jenkins.baseUrl}")
    private static String baseUrl;
    @Value("${jenkins.username}")
    private static String username;
    @Value("${jenkins.password}")
    private static String password;
    private final RestTemplate restTemplate;
    public JenkinsService() {
        this.restTemplate = new RestTemplate();
    }

/////////////////////////////////////////////////
public List<String> getAllJobNames() throws JsonProcessingException {


    String JenkinsUrl = "http://localhost:8080/";
    String username = "admin";
    String password = "admin";

    String url = JenkinsUrl + "api/json?tree=jobs[name]";


    // Encode credentials
    String plainCredentials = username + ":" + password;
    String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));

    // Create headers with Authorization header
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + encodedCredentials);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> requestEntity = new HttpEntity<>(headers);

    // Send the request and retrieve the response
    ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

    HttpStatusCode responseStatus = responseEntity.getStatusCode();
    String responseBody = responseEntity.getBody();

    if (responseStatus == HttpStatus.OK) {
        // Extract the job names from the response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode jobsNode = rootNode.get("jobs");
        List<String> jobNames = new ArrayList<>();

        for (JsonNode jobNode : jobsNode) {
            String jobName = jobNode.get("name").asText();
            jobNames.add(jobName);
        }

        return jobNames;
    } else {
        throw new RuntimeException("Failed to fetch job data from Jenkins. Status code: " + responseStatus);
    }
}
//////////////////////////////////////////////////////////////////////////
    private String getJobStatusFromJenkins(JsonNode rootNode) {
        JsonNode colorNode = rootNode.get("color");
        if (colorNode != null && colorNode.isTextual()) {
            String color = colorNode.asText();
            if (color.contains("blue")) {
                return "SUCCESS";
            } else if (color.contains("yellow")) {
                return "UNSTABLE";
            } else if (color.contains("red")) {
                return "FAILURE";
            } else if (color.contains("aborted")) {
                return "ABORTED";
            } else if (color.contains("disabled")) {
                return "DISABLED";
            }
        }

        JsonNode resultNode = rootNode.get("result");
        if (resultNode != null && resultNode.isTextual()) {
            String result = resultNode.asText();
            if (result.equals("SUCCESS")) {
                return "SUCCESS";
            } else if (result.equals("UNSTABLE")) {
                return "UNSTABLE";
            } else if (result.equals("FAILURE")) {
                return "FAILURE";
            } else if (result.equals("ABORTED")) {
                return "ABORTED";
            }
        }

        return "UNKNOWN";
    }

    public JenkinsJobBuild getLatestJobBuild() throws JsonProcessingException {


        String JenkinsUrl = "http://localhost:8080/";
        String username = "admin";
        String password = "admin";

        String url = JenkinsUrl + "job/project_jenkins/lastBuild/api/json";

        // Encode credentials
        String plainCredentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));
        // Create headers with Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        // Send the request and retrieve the response
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);


        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        String responseBody = responseEntity.getBody();

        if (responseStatus == HttpStatus.OK) {

            // Extract the values
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            String fullDisplayName = rootNode.get("fullDisplayName").asText();
            // Split the string by the '#' character
            String[] parts = fullDisplayName.split("#");
            // Extract the JobName
            String jobName = parts[0].trim();
            // Extract the BuildNumber
            int buildNumber = Integer.parseInt(parts[1].trim());

            // Extract the timestamp
            String timestampS = rootNode.get("timestamp").asText();
            long timestampI =Long.parseLong(timestampS);
            Instant instant = Instant.ofEpochMilli(timestampI);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

            String jobDuration = rootNode.get("duration").asText();

            // Retrieve the job status from Jenkins
            String jobStatus = getJobStatusFromJenkins(rootNode);

            // create an instance of JenkinsJobBuild
            JenkinsJobBuild jobBuild = new JenkinsJobBuild();
            jobBuild.setJobName(jobName);
            jobBuild.setdateTime(dateTime);
            jobBuild.setBuildNumber(buildNumber);
            jobBuild.setjobDuration(jobDuration);
            jobBuild.setJobStatus(jobStatus); // Set the job status

            return jobBuild;

        } else {
            System.out.println("Request failed with status code: " + responseStatus);
        }
        return null;
    }




    public List<JenkinsJobBuild> getJobBuildsByTimeRange(LocalDateTime startTime, LocalDateTime endTime,String TheJobName) throws JsonProcessingException {


        String JenkinsUrl = "http://localhost:8080/";
        String username = "admin";
        String password = "admin";

        String url = JenkinsUrl + "job/"+ TheJobName +"/api/json?tree=allBuilds[id,fullDisplayName,timestamp,duration]";


        // Encode credentials
        String plainCredentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));

        // Create headers with Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Send the request and retrieve the response
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        String responseBody = responseEntity.getBody();

        if (responseStatus == HttpStatus.OK) {
            // Extract the values
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            List<JenkinsJobBuild> jobBuilds = new ArrayList<>();

            // Iterate over the builds

            for (JsonNode buildNode : rootNode.get("allBuilds")) {
                String fullDisplayName = buildNode.get("fullDisplayName").asText();
                // Split the string by the '#' character
                String[] parts = fullDisplayName.split("#");
                // Extract the JobName
                String jobName = parts[0].trim();
                // Extract the BuildNumber
                int buildNumber = Integer.parseInt(parts[1].trim());

                // Extract the timestamp
                String timestampS = buildNode.get("timestamp").asText();
                long timestampI = Long.parseLong(timestampS);
                Instant instant = Instant.ofEpochMilli(timestampI);
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                String jobDuration = buildNode.get("duration").asText();

                // Check if the build falls within the specified time range
                if (dateTime.isAfter(startTime) && dateTime.isBefore(endTime)) {
                    // create an instance of JenkinsJobBuild
                    JenkinsJobBuild jobBuild = new JenkinsJobBuild();
                    jobBuild.setJobName(jobName);
                    jobBuild.setdateTime(dateTime);
                    jobBuild.setBuildNumber(buildNumber);
                    jobBuild.setjobDuration(jobDuration);
                    jobBuilds.add(jobBuild);
                }
            }

            return jobBuilds;

        } else {
            System.out.println("Request failed with status code: " + responseStatus);
        }
        return Collections.emptyList();
    }

//////////////////////////////////////////////////////////////////////////
    public static List<JenkinsJobBuild> getJobBuildsByTimeRange2(LocalDateTime startTime, LocalDateTime endTime,String TheJobName) throws Exception {


        String JenkinsUrl = "http://localhost:8080/";
        String username = "admin";
        String password = "admin";
        // i can extract all the job names but not the job names working on a specific node !

        String url = JenkinsUrl + "job/"+ TheJobName +"/api/json?pretty=true&depth=2";

        // Encode credentials
        String plainCredentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));

        // Create headers with Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send the request and retrieve the response
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        String responseBody = responseEntity.getBody();

        if (responseStatus == HttpStatus.OK) {
            // Extract the values
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            List<JenkinsJobBuild> jobBuilds = new ArrayList<>();

            // Iterate over the builds
            JsonNode buildsNode = rootNode.get("builds");
            if (buildsNode.isArray()) {

                for (JsonNode buildNode : buildsNode) {
                    JenkinsJobBuild jobBuild = new JenkinsJobBuild();

                    // Extract build information
                    jobBuild.setJobName(rootNode.get("displayName").asText());
                    jobBuild.setQueuingDuration(buildNode.path("actions").get(1).path("queuingDurationMillis").asText());
                    jobBuild.setBuildNumber(buildNode.path("number").asInt());
                    jobBuild.setJobStatus(buildNode.path("result").asText());
                    jobBuild.setjobDuration(buildNode.path("duration").asText());
                    String timestampS=buildNode.path("timestamp").asText();

                    //Converting to local dataTime type:
                    long timestampI =Long.parseLong(timestampS);
                    Instant instant = Instant.ofEpochMilli(timestampI);
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    jobBuild.setdateTime(dateTime);


                    LocalDateTime TheEndTime=jobBuild.CalculateTheEndTime();
                    jobBuild.setTheEndTime(TheEndTime);

                    LocalDateTime executionDate=jobBuild.CalculateExecutionDate();
                    jobBuild.setExecutionDate(executionDate);

                    if (dateTime.isAfter(startTime) && dateTime.isBefore(endTime)) {
                        jobBuilds.add(jobBuild);
                    }
                }
            }

            return jobBuilds;
        } else {
            throw new Exception("Failed to retrieve job builds. Status: " + responseStatus);
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////


    public ResponseEntity<Object> getallJobInfo() {
        try {
            String JenkinsUrl = "http://localhost:8080/";
            String username = "admin";
            String password = "admin";

            String url = JenkinsUrl + "api/json?tree=jobs[name,builds[number,builtOn]]";

            // Encode credentials
            String plainCredentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));

            // Create headers with Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Send the request and retrieve the response
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            HttpStatusCode responseStatus = responseEntity.getStatusCode();
            String responseBody = responseEntity.getBody();

            List<Map<String, String>> jobInfoList = new ArrayList<>();

            if (responseStatus == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode jobsNode = rootNode.get("jobs");

                for (JsonNode jobNode : jobsNode) {
                    String jobName = jobNode.get("name").asText();

                    JsonNode buildsNode = jobNode.get("builds");
                    for (JsonNode buildNode : buildsNode) {
                        String number = buildNode.get("number").asText();
                        String builtOn = buildNode.has("builtOn") ? buildNode.get("builtOn").asText() : "-";

                        Map<String, String> jobInfo = new HashMap<>();
                        jobInfo.put("name", jobName);
                        jobInfo.put("number", number);
                        jobInfo.put("builtOn", builtOn);
                        jobInfoList.add(jobInfo);
                    }
                }

                return ResponseEntity.ok().body(jobInfoList);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Failed to fetch job data from Jenkins. Status code: " + responseStatus));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }


///////////////////////////////////////////////////////////////////////////////////////////

    public List<String> getNodesNames() throws JsonProcessingException {


        String JenkinsUrl = "http://localhost:8080/";
        String username = "admin";
        String password = "admin";

        String url = JenkinsUrl+"computer/api/json";

        // Encode credentials
        String plainCredentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));
        // Create headers with Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        // Send the request and retrieve the response
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);


        HttpStatusCode responseStatus = responseEntity.getStatusCode();
        String responseBody = responseEntity.getBody();

        if (responseStatus == HttpStatus.OK) {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            List<String> nodesList = new ArrayList<>();

            for (JsonNode Node : rootNode.get("computer")) {
                String displayName = Node.get("displayName").asText();
                nodesList.add(displayName);
            }
            return nodesList;

        } else {
            System.out.println("Request failed with status code: " + responseStatus);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
}
