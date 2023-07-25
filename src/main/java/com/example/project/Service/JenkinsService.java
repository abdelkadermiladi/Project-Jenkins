package com.example.project.Service;
import com.example.project.Model.JenkinsJobBuild;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final RestTemplate restTemplate;
    public JenkinsService() {
        System.out.println("start JenkinsService");

        this.restTemplate = new RestTemplate();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    public HttpHeaders getAuthHeadersJenkins(String username, String password) {
        try {
            // Encode credentials
            String plainCredentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes(StandardCharsets.UTF_8));

            // Create headers with Authorization header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Perform a test request to Jenkins to check authentication
            // If the request succeeds, it means the provided credentials are valid
            String testUrl = "http://localhost:8080/api/json";
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(testUrl, HttpMethod.GET, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // Return the headers if authentication is successful
                return headers;
            } else {
                // If authentication fails, return null or throw an exception, based on your requirements
                return null;
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur during authentication
            // For example, log the error and return null or throw an exception
            e.printStackTrace();
            return null;
        } finally {
            // Clear sensitive data
            password = null;
        }
    }




    ///////////////////////////////////////////////////////////////////////////////////////
    //retrieve all the job name from jenkins server
    public List<String> getAllJobNames(HttpHeaders headers) throws JsonProcessingException {

        String JenkinsUrl="http://localhost:8080/";

        String url = JenkinsUrl+"api/json?tree=jobs[name]";

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

    ///////////////////////////////////////////////////////////////////////////////////////
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


    ///////////////////////////////////////////////////////////////////////////////////////

    public JenkinsJobBuild getLatestJobBuild(HttpHeaders headers) throws JsonProcessingException {
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        String JenkinsUrl = "http://localhost:8080/";

        List<String> allJobNames = getAllJobNames(headers);
        JenkinsJobBuild latestJobBuild = null;
        LocalDateTime latestDateTime = LocalDateTime.MIN; // Initialize to the smallest possible value

        for (String jobName : allJobNames) {
            String url = JenkinsUrl + "job/" + jobName + "/lastBuild/api/json";

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
                // Extract the BuildNumber
                int buildNumber = Integer.parseInt(parts[1].trim());

                // Extract the timestamp
                String timestampS = rootNode.get("timestamp").asText();
                long timestampI = Long.parseLong(timestampS);
                Instant instant = Instant.ofEpochMilli(timestampI);
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                if (dateTime.isAfter(latestDateTime)) {
                    // If the current build has a more recent datetime, update the latestJobBuild
                    latestDateTime = dateTime;

                    // Retrieve other job information (e.g., job name, duration, status)
                    String jobDuration = rootNode.get("duration").asText();
                    String jobStatus = getJobStatusFromJenkins(rootNode);

                    // Create an instance of JenkinsJobBuild
                    latestJobBuild = new JenkinsJobBuild();
                    latestJobBuild.setJobName(jobName);
                    latestJobBuild.setdateTime(dateTime);
                    latestJobBuild.setBuildNumber(buildNumber);
                    latestJobBuild.setjobDuration(jobDuration);
                    latestJobBuild.setJobStatus(jobStatus); // Set the job status
                }
            } else {
                System.out.println("Request failed with status code: " + responseStatus);
            }
        }

        return latestJobBuild;
    }




    ///////////////////////////////////////////////////////////////////////////////////////
    //This method will be used to retrieve last hour job builds
    public List<JenkinsJobBuild> getJobBuildsByTimeRange1(HttpHeaders headers,LocalDateTime startTime, LocalDateTime endTime,String TheJobName) throws JsonProcessingException {

        String JenkinsUrl = "http://localhost:8080/";
        String url = JenkinsUrl+"job/"+ TheJobName +"/api/json?tree=allBuilds[id,fullDisplayName,timestamp,duration]";

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

    ///////////////////////////////////////////////////////////////////////////////////////
    //This method will be used to retrieve time range job builds
    public List<JenkinsJobBuild> getJobBuildsByTimeRange2(HttpHeaders headers,LocalDateTime startTime, LocalDateTime endTime,String TheJobName) throws Exception {

        String JenkinsUrl = "http://localhost:8080/";

        String url = JenkinsUrl + "job/"+ TheJobName +"/api/json?pretty=true&depth=2";


        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();


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

    ///////////////////////////////////////////////////////////////////////////////////////
    //retrieve all {JobName , buildNumber, NodeName}
    public ResponseEntity<Object> getallJobInfo(HttpHeaders headers) {
        try {
            String JenkinsUrl = "http://localhost:8080/";

            String url = JenkinsUrl + "api/json?tree=jobs[name,builds[number,builtOn]]";


            // Create RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();


            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

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

    ///////////////////////////////////////////////////////////////////////////////////////
    //Retrieve all NodeNames in Jenkins Server
    public List<String> getNodesNames(HttpHeaders headers) throws JsonProcessingException {


        String JenkinsUrl = "http://localhost:8080/";

        String url = JenkinsUrl+"computer/api/json";

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
