package com.studentcloudhub.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {

    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;
    private int selectedAnswerIndex = -1; // -1 means unanswered

    public Question() {
        this.options = new ArrayList<>();
    }

    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options != null ? options : new ArrayList<>();
        this.correctAnswerIndex = correctAnswerIndex;
        this.selectedAnswerIndex = -1;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public int getSelectedAnswerIndex() {
        return selectedAnswerIndex;
    }

    public void setSelectedAnswerIndex(int selectedAnswerIndex) {
        this.selectedAnswerIndex = selectedAnswerIndex;
    }

    public boolean isAnswered() {
        return selectedAnswerIndex >= 0 && selectedAnswerIndex < options.size();
    }

    public boolean isCorrect() {
        return isAnswered() && selectedAnswerIndex == correctAnswerIndex;
    }

    public String getCorrectAnswerText() {
        if (correctAnswerIndex >= 0 && correctAnswerIndex < options.size()) {
            return options.get(correctAnswerIndex);
        }
        return "";
    }

    public String getSelectedAnswerText() {
        if (selectedAnswerIndex >= 0 && selectedAnswerIndex < options.size()) {
            return options.get(selectedAnswerIndex);
        }
        return "Not Answered";
    }
}
