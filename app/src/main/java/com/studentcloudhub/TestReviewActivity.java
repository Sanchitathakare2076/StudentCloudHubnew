package com.studentcloudhub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.studentcloudhub.adapter.TestReviewAdapter;
import com.studentcloudhub.model.Question;
import com.studentcloudhub.model.TestModel;

import java.util.ArrayList;
import java.util.List;

public class TestReviewActivity extends AppCompatActivity {

    private RecyclerView rvReview;
    private TestReviewAdapter adapter;
    private List<Question> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_review);

        rvReview = findViewById(R.id.rvReview);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (getIntent().hasExtra(TestActivity.EXTRA_TEST_MODEL)) {
            TestModel testModel = (TestModel) getIntent().getSerializableExtra(TestActivity.EXTRA_TEST_MODEL);
            if (testModel != null && testModel.getQuestionList() != null) {
                questionList = testModel.getQuestionList();
            }
        }

        if (questionList.isEmpty()) {
            Toast.makeText(this, "No review questions available", Toast.LENGTH_SHORT).show();
        }

        rvReview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TestReviewAdapter(this, questionList);
        rvReview.setAdapter(adapter);
    }
}
