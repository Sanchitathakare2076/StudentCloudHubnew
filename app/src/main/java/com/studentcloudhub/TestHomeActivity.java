package com.studentcloudhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class TestHomeActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT = "extra_subject";
    public static final String EXTRA_AMOUNT = "extra_amount";

    private Spinner spinnerSubject;
    private RadioGroup rgQuestionCount;
    private RadioButton rb5, rb10, rb15;
    private MaterialButton btnStartTest;

    private static final String[] SUBJECTS = {
            "Computer Science",
            "Mathematics",
            "Science",
            "General Knowledge",
            "History"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_home);

        spinnerSubject = findViewById(R.id.spinnerSubject);
        rgQuestionCount = findViewById(R.id.rgQuestionCount);
        rb5 = findViewById(R.id.rb5);
        rb10 = findViewById(R.id.rb10);
        rb15 = findViewById(R.id.rb15);
        btnStartTest = findViewById(R.id.btnStartTest);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                SUBJECTS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        btnStartTest.setOnClickListener(v -> {
            String selectedSubject = (String) spinnerSubject.getSelectedItem();

            int amount = 10;
            int checkedId = rgQuestionCount.getCheckedRadioButtonId();
            if (checkedId == R.id.rb5) {
                amount = 5;
            } else if (checkedId == R.id.rb15) {
                amount = 15;
            }

            Intent intent = new Intent(TestHomeActivity.this, TestActivity.class);
            intent.putExtra(EXTRA_SUBJECT, selectedSubject);
            intent.putExtra(EXTRA_AMOUNT, amount);
            startActivity(intent);
        });
    }
}
