package com.studentcloudhub.model;

import java.io.Serializable;

public class StudyPlannerModel implements Serializable {

    private String id;
    private String userId;
    private String subject;
    private String topic;
    private String date;
    private String startTime;
    private String endTime;
    private String priority;
    private String description;
    private String status; // "Pending" or "Completed"
    private long createdAt;

    public StudyPlannerModel() {
        // Required for Firestore
    }

    public StudyPlannerModel(String id, String userId, String subject, String topic, String date,
                             String startTime, String endTime, String priority, String description,
                             String status, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.subject = subject;
        this.topic = topic;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }
}
