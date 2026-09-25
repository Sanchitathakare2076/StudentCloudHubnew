package com.studentcloudhub;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.studentcloudhub.api.ApiService;
import com.studentcloudhub.model.Question;
import com.studentcloudhub.model.TestModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    public static final String EXTRA_TEST_MODEL = "extra_test_model";

    private TextView tvTestSubject;
    private TextView tvTimer;
    private TextView tvQuestionCount;
    private TextView tvQuestionText;
    private TextView tvErrorMessage;
    private ProgressBar testProgressBar;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private LinearLayout layoutTestContent;
    private RadioGroup rgOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private MaterialButton btnPrevious, btnNext, btnRetry;

    private String subject = "General";
    private int requestedAmount = 10;
    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;

    private CountDownTimer countDownTimer;
    private long timeRemainingMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if (getIntent().hasExtra(TestHomeActivity.EXTRA_SUBJECT)) {
            subject = getIntent().getStringExtra(TestHomeActivity.EXTRA_SUBJECT);
        }
        if (getIntent().hasExtra(TestHomeActivity.EXTRA_AMOUNT)) {
            requestedAmount = getIntent().getIntExtra(TestHomeActivity.EXTRA_AMOUNT, 10);
        }

        tvTestSubject = findViewById(R.id.tvTestSubject);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        testProgressBar = findViewById(R.id.testProgressBar);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        layoutTestContent = findViewById(R.id.layoutTestContent);
        rgOptions = findViewById(R.id.rgOptions);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnRetry = findViewById(R.id.btnRetry);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

        tvTestSubject.setText(subject);
        findViewById(R.id.btnBack).setOnClickListener(v -> showExitConfirmationDialog());
        btnRetry.setOnClickListener(v -> loadQuestions());

        btnPrevious.setOnClickListener(v -> navigatePrevious());
        btnNext.setOnClickListener(v -> navigateNext());

        rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (currentQuestionIndex >= 0 && currentQuestionIndex < questionList.size()) {
                Question q = questionList.get(currentQuestionIndex);
                if (checkedId == R.id.rbOption1) {
                    q.setSelectedAnswerIndex(0);
                } else if (checkedId == R.id.rbOption2) {
                    q.setSelectedAnswerIndex(1);
                } else if (checkedId == R.id.rbOption3) {
                    q.setSelectedAnswerIndex(2);
                } else if (checkedId == R.id.rbOption4) {
                    q.setSelectedAnswerIndex(3);
                }
            }
        });

        loadQuestions();
    }

    private void loadQuestions() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutTestContent.setVisibility(View.GONE);

        ApiService.fetchQuestions(subject, requestedAmount, new ApiService.QuestionsCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                if (isFinishing()) return;

                if (questions == null || questions.isEmpty()) {
                    showErrorState("No questions found for " + subject + ". Please try again.");
                    return;
                }

                questionList = questions;
                currentQuestionIndex = 0;

                layoutLoading.setVisibility(View.GONE);
                layoutError.setVisibility(View.GONE);
                layoutTestContent.setVisibility(View.VISIBLE);

                startTimer(questionList.size() * 60 * 1000L); // 1 minute per question
                displayQuestion(currentQuestionIndex);
            }

            @Override
            public void onError(String errorMessage) {
                if (isFinishing()) return;
                showErrorState("Network / API Error: " + errorMessage);
            }
        });
    }

    private void showErrorState(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutTestContent.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(message);
    }

    private void startTimer(long durationMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMillis = millisUntilFinished;
                long seconds = (millisUntilFinished / 1000) % 60;
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "⏱️ %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("⏱️ 00:00");
                Toast.makeText(TestActivity.this, "Time is up! Submitting test...", Toast.LENGTH_SHORT).show();
                submitTest();
            }
        };
        countDownTimer.start();
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= questionList.size()) return;

        Question q = questionList.get(index);

        tvQuestionCount.setText("Question " + (index + 1) + " of " + questionList.size());
        testProgressBar.setMax(questionList.size());
        testProgressBar.setProgress(index + 1);

        tvQuestionText.setText(q.getQuestionText());

        List<String> options = q.getOptions();
        rgOptions.clearCheck();

        rbOption1.setText(options.size() > 0 ? options.get(0) : "");
        rbOption2.setText(options.size() > 1 ? options.get(1) : "");
        rbOption3.setText(options.size() > 2 ? options.get(2) : "");
        rbOption4.setText(options.size() > 3 ? options.get(3) : "");

        int selectedIndex = q.getSelectedAnswerIndex();
        if (selectedIndex == 0) rbOption1.setChecked(true);
        else if (selectedIndex == 1) rbOption2.setChecked(true);
        else if (selectedIndex == 2) rbOption3.setChecked(true);
        else if (selectedIndex == 3) rbOption4.setChecked(true);

        btnPrevious.setEnabled(index > 0);

        if (index == questionList.size() - 1) {
            btnNext.setText("Submit Test");
        } else {
            btnNext.setText("Next");
        }
    }

    private void navigatePrevious() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuestion(currentQuestionIndex);
        }
    }

    private void navigateNext() {
        if (currentQuestionIndex < questionList.size() - 1) {
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
        } else {
            // Last question clicked "Submit Test"
            showSubmitConfirmationDialog();
        }
    }

    private void showSubmitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Submit Test")
                .setMessage("Are you sure you want to submit your test?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", (dialog, which) -> submitTest())
                .show();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Test")
                .setMessage("Are you sure you want to exit? Your test progress will be lost.")
                .setNegativeButton("No", null)
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .show();
    }

    private void submitTest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        TestModel testModel = new TestModel(subject, questionList.size(), questionList);

        Intent intent = new Intent(TestActivity.this, TestResultActivity.class);
        intent.putExtra(EXTRA_TEST_MODEL, testModel);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
