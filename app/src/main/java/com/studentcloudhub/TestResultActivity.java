package com.studentcloudhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studentcloudhub.model.Question;
import com.studentcloudhub.model.TestModel;
import com.studentcloudhub.model.TestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TestResultActivity extends AppCompatActivity {

    private static final String TAG = "TestResultActivity";

    private TextView tvResultSubject;
    private TextView tvPercentage;
    private TextView tvScoreMessage;
    private TextView tvScoreSummary;
    private TextView tvTotalQuestions;
    private TextView tvCorrectAnswers;
    private TextView tvWrongAnswers;
    private MaterialButton btnReviewAnswers;
    private MaterialButton btnDone;

    private TestModel testModel;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvResultSubject = findViewById(R.id.tvResultSubject);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvScoreMessage = findViewById(R.id.tvScoreMessage);
        tvScoreSummary = findViewById(R.id.tvScoreSummary);
        tvTotalQuestions = findViewById(R.id.tvTotalQuestions);
        tvCorrectAnswers = findViewById(R.id.tvCorrectAnswers);
        tvWrongAnswers = findViewById(R.id.tvWrongAnswers);
        btnReviewAnswers = findViewById(R.id.btnReviewAnswers);
        btnDone = findViewById(R.id.btnDone);

        if (getIntent().hasExtra(TestActivity.EXTRA_TEST_MODEL)) {
            testModel = (TestModel) getIntent().getSerializableExtra(TestActivity.EXTRA_TEST_MODEL);
        }

        if (testModel != null) {
            calculateAndDisplayResult();
        } else {
            Toast.makeText(this, "No test result data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnReviewAnswers.setOnClickListener(v -> {
            Intent intent = new Intent(TestResultActivity.this, TestReviewActivity.class);
            intent.putExtra(TestActivity.EXTRA_TEST_MODEL, testModel);
            startActivity(intent);
        });

        btnDone.setOnClickListener(v -> finish());
    }

    private void calculateAndDisplayResult() {
        String subject = testModel.getSubject() != null ? testModel.getSubject() : "Test";
        List<Question> questions = testModel.getQuestionList();
        int total = questions != null ? questions.size() : 0;

        int correct = 0;
        if (questions != null) {
            for (Question q : questions) {
                if (q.isCorrect()) {
                    correct++;
                }
            }
        }
        int wrong = total - correct;
        double percentage = total > 0 ? (correct * 100.0) / total : 0;
        int score = correct; // 1 point per question

        tvResultSubject.setText(subject);
        tvTotalQuestions.setText(String.valueOf(total));
        tvCorrectAnswers.setText(String.valueOf(correct));
        tvWrongAnswers.setText(String.valueOf(wrong));
        tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
        tvScoreSummary.setText("You scored " + correct + " out of " + total);

        if (percentage >= 80) {
            tvScoreMessage.setText("🎉 Excellent Performance!");
        } else if (percentage >= 50) {
            tvScoreMessage.setText("👍 Good Effort! Keep Learning!");
        } else {
            tvScoreMessage.setText("📖 Keep Practicing to Improve!");
        }

        saveTestResultToFirestore(subject, total, correct, wrong, score, percentage);
    }

    private void saveTestResultToFirestore(String subject, int total, int correct, int wrong, int score, double percentage) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String testId = UUID.randomUUID().toString();

        Map<String, Object> map = new HashMap<>();
        map.put("userId", uid);
        map.put("testId", testId);
        map.put("subject", subject);
        map.put("totalQuestions", total);
        map.put("correctAnswers", correct);
        map.put("wrongAnswers", wrong);
        map.put("score", score);
        map.put("percentage", Math.round(percentage * 10.0) / 10.0);
        map.put("completedAt", System.currentTimeMillis());

        db.collection("test_results")
                .add(map)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Test result saved to Firestore: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save test result", e));
    }
}
