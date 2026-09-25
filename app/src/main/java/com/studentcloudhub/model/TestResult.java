package com.studentcloudhub.model;

import java.io.Serializable;

public class TestResult implements Serializable {

    private String id;
    private String userId;
    private String testId;
    private String subject;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int score;
    private double percentage;
    private long completedAt;

    public TestResult() {
        // Required for Firestore
    }

    public TestResult(String id, String userId, String testId, String subject,
                      int totalQuestions, int correctAnswers, int wrongAnswers,
                      int score, double percentage, long completedAt) {
        this.id = id;
        this.userId = userId;
        this.testId = testId;
        this.subject = subject;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.score = score;
        this.percentage = percentage;
        this.completedAt = completedAt;
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

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}
