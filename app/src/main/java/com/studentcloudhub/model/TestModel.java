package com.studentcloudhub.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestModel implements Serializable {

    private String subject;
    private int totalQuestions;
    private List<Question> questionList;

    public TestModel() {
        this.questionList = new ArrayList<>();
    }

    public TestModel(String subject, int totalQuestions, List<Question> questionList) {
        this.subject = subject;
        this.totalQuestions = totalQuestions;
        this.questionList = questionList != null ? questionList : new ArrayList<>();
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

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }
}
