package com.example.project;

import java.time.LocalDateTime;

public class JenkinsJobBuild {
    private String jobName;
    private LocalDateTime dateTime;
    private int buildNumber;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public LocalDateTime getdateTime() {
        return dateTime;
    }

    public void setdateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }
}
