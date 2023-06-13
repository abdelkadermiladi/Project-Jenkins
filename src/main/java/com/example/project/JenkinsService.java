package com.example.project;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.print.attribute.standard.JobName;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class JenkinsService {
    @Value("${jenkins.baseUrl}")
    private String baseUrl;
    @Value("${jenkins.username}")
    private String username;
    @Value("${jenkins.password}")
    private String password;
    private final RestTemplate restTemplate;
    public JenkinsService() {
        this.restTemplate = new RestTemplate();
    }
    public String getLatestJobBuild() throws JsonProcessingException {
        String url = baseUrl + "job/project_jenkins/lastBuild/api/json";
        //String jobName = "project_jenkins"; // Replace with your job name

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
            String timestamp = rootNode.get("timestamp").asText();

            System.out.println("\n");
            System.out.println("Response Body: " + responseBody);
            System.out.println("\n");
            System.out.println("full display name : " + fullDisplayName);
            System.out.println("Job name : " + jobName);
            System.out.println("Build number : " + buildNumber);
            System.out.println("Timestamp : " + timestamp);



            JenkinsJobBuild jobBuild = new JenkinsJobBuild();
            jobBuild.setJobName(jobName);
            jobBuild.setTimestamp(timestamp);
            jobBuild.setBuildNumber(buildNumber);

        } else {
            System.out.println("Request failed with status code: " + responseStatus);
        }
        return responseBody;
    }
}
