package com.example.project.Model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class JenkinsJobBuild {
    private String jobName;

    private LocalDateTime dateTime;    //Start Date

    private LocalDateTime TheEndTime;

    private LocalDateTime ExecutionDate;

    private int buildNumber;

    private String jobDuration;

    private String jobStatus;

    private String queuingDuration;



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

    public String getjobDuration() {
        return jobDuration;
    }

    public void setjobDuration(String jobDuration) {
        this.jobDuration = jobDuration;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getQueuingDuration() {
        return queuingDuration;
    }

    public void setQueuingDuration(String queuingDuration) {
        this.queuingDuration = queuingDuration;
    }



    public LocalDateTime CalculateTheEndTime() {
        //EndTime=StartDate+Duration
        String durationString = this.jobDuration;
        durationString = durationString.replaceAll("[^\\d]", ""); // Supprimer tous les caractères non numériques
        long durationMillis = Long.parseLong(durationString);
        LocalDateTime EndTime = this.dateTime.plus(durationMillis, ChronoUnit.MILLIS);
        this.TheEndTime = EndTime;
        return EndTime;
    }

    public LocalDateTime getTheEndTime() {
        return TheEndTime;
    }

    public void setTheEndTime(LocalDateTime theEndTime) {
        TheEndTime = theEndTime;
    }

    public LocalDateTime CalculateExecutionDate() {
        //ExecutionDate=StartDate+QueuingDuration
        String queuingDuration = this.queuingDuration;
        if (queuingDuration.isEmpty())
        {
            //we don't have the value of queuing duration
            LocalDateTime executionDate = this.dateTime;
            return executionDate;
        }
        else
        {
            queuingDuration = queuingDuration.replaceAll("[^\\d]", ""); // Supprimer tous les caractères non numériques
            long durationMillis = Long.parseLong(queuingDuration);
            LocalDateTime executionDate = this.dateTime.plus(durationMillis, ChronoUnit.MILLIS);
            this.ExecutionDate = executionDate;
            return executionDate;
        }
    }


    public LocalDateTime getExecutionDate() {
        return ExecutionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        ExecutionDate = executionDate;
    }
}
