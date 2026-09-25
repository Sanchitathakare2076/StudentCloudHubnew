package com.studentcloudhub;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AIActivity extends AppCompatActivity {

    private EditText etQuestion;
    private FloatingActionButton btnSend;
    private TextView tvAnswer;
    private View answerCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_ai);

        etQuestion = findViewById(R.id.etQuestion);
        btnSend = findViewById(R.id.btnSend);
        tvAnswer = findViewById(R.id.tvAnswer);
        answerCard = findViewById(R.id.answerCard);

        btnSend.setOnClickListener(view -> {

            String question = etQuestion
                    .getText()
                    .toString()
                    .trim();

            if (question.isEmpty()) {

                etQuestion.setError(
                        "Please enter your question"
                );

                return;
            }

            String answer = getTutorAnswer(question);

            tvAnswer.setText(answer);
            if (answerCard != null) {
                answerCard.setVisibility(View.VISIBLE);
            }

            etQuestion.setText("");
        });
    }

    private String getTutorAnswer(String question) {

        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("hello")
                || lowerQuestion.contains("hi")) {

            return "Hello Student! How can I help you with your studies?";

        } else if (lowerQuestion.contains("cloud computing")) {

            return "Cloud Computing is the delivery of computing services "
                    + "such as servers, storage, databases and software "
                    + "through the internet.";

        } else if (lowerQuestion.contains("java")) {

            return "Java is an object-oriented programming language. "
                    + "It is used to develop Android applications, "
                    + "web applications and desktop software.";

        } else if (lowerQuestion.contains("android")) {

            return "Android is a mobile operating system. "
                    + "Android applications can be developed using "
                    + "Java and XML in Android Studio.";

        } else if (lowerQuestion.contains("oops")
                || lowerQuestion.contains("oops concepts")) {

            return "The main OOP concepts are:\n\n"
                    + "1. Class\n"
                    + "2. Object\n"
                    + "3. Encapsulation\n"
                    + "4. Inheritance\n"
                    + "5. Polymorphism\n"
                    + "6. Abstraction";

        } else if (lowerQuestion.contains("fcfs")) {

            return "FCFS means First Come First Serve. "
                    + "The process that arrives first gets the CPU first. "
                    + "It is a non-preemptive scheduling algorithm.";

        } else {

            return "You asked:\n\n"
                    + question
                    + "\n\nThis is the demo AI Tutor. "
                    + "Connect an AI API or backend to receive "
                    + "real AI-generated answers.";
        }
    }
}