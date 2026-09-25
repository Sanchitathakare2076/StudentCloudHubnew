package com.studentcloudhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.studentcloudhub.adapter.StudyPlannerAdapter;
import com.studentcloudhub.model.StudyPlannerModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudyPlannerActivity extends AppCompatActivity implements StudyPlannerAdapter.OnTaskActionListener {

    private enum FilterMode {
        ALL, TODAY, UPCOMING
    }

    private FilterMode currentFilter = FilterMode.ALL;

    private TextView tvProgressPercent;
    private TextView tvCompletedCount;
    private TextView tvPendingCount;
    private TextView tvEmpty;
    private TextView tabAll, tabToday, tabUpcoming;
    private ProgressBar progressBar;
    private RecyclerView rvStudyPlans;
    private FloatingActionButton fabAddStudyPlan;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<StudyPlannerModel> allTasks = new ArrayList<>();
    private final List<StudyPlannerModel> displayedTasks = new ArrayList<>();
    private StudyPlannerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_planner);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        tabAll = findViewById(R.id.tabAll);
        tabToday = findViewById(R.id.tabToday);
        tabUpcoming = findViewById(R.id.tabUpcoming);
        progressBar = findViewById(R.id.progressBar);
        rvStudyPlans = findViewById(R.id.rvStudyPlans);
        fabAddStudyPlan = findViewById(R.id.fabAddStudyPlan);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvStudyPlans.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudyPlannerAdapter(this, displayedTasks, this);
        rvStudyPlans.setAdapter(adapter);

        tabAll.setOnClickListener(v -> setFilter(FilterMode.ALL));
        tabToday.setOnClickListener(v -> setFilter(FilterMode.TODAY));
        tabUpcoming.setOnClickListener(v -> setFilter(FilterMode.UPCOMING));

        fabAddStudyPlan.setOnClickListener(v -> {
            Intent intent = new Intent(StudyPlannerActivity.this, AddStudyPlanActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudyPlans();
    }

    private void loadStudyPlans() {
        if (auth.getCurrentUser() == null) {
            tvEmpty.setText("Please log in to view study tasks.");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        tvEmpty.setText("Loading study plans...");
        tvEmpty.setVisibility(View.VISIBLE);

        db.collection("study_plans")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allTasks.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        StudyPlannerModel model = new StudyPlannerModel(
                                doc.getId(),
                                doc.getString("userId"),
                                doc.getString("subject"),
                                doc.getString("topic"),
                                doc.getString("date"),
                                doc.getString("startTime"),
                                doc.getString("endTime"),
                                doc.getString("priority"),
                                doc.getString("description"),
                                doc.getString("status"),
                                doc.contains("createdAt") && doc.getLong("createdAt") != null ? doc.getLong("createdAt") : 0
                        );
                        allTasks.add(model);
                    }

                    // Sort tasks by Date and Time
                    Collections.sort(allTasks, (t1, t2) -> {
                        String d1 = t1.getDate() != null ? t1.getDate() : "";
                        String d2 = t2.getDate() != null ? t2.getDate() : "";
                        int cmp = d1.compareTo(d2);
                        if (cmp != 0) return cmp;

                        String time1 = t1.getStartTime() != null ? t1.getStartTime() : "";
                        String time2 = t2.getStartTime() != null ? t2.getStartTime() : "";
                        return time1.compareTo(time2);
                    });

                    updateProgressStats();
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    tvEmpty.setText("Failed to load study tasks: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void updateProgressStats() {
        int total = allTasks.size();
        int completed = 0;
        for (StudyPlannerModel task : allTasks) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        int pending = total - completed;
        int percent = total > 0 ? (completed * 100) / total : 0;

        tvCompletedCount.setText("Completed: " + completed);
        tvPendingCount.setText("Pending: " + pending);
        tvProgressPercent.setText(percent + "%");
        progressBar.setProgress(percent);
    }

    private void setFilter(FilterMode mode) {
        currentFilter = mode;

        tabAll.setTextColor(Color.parseColor("#616161"));
        tabToday.setTextColor(Color.parseColor("#616161"));
        tabUpcoming.setTextColor(Color.parseColor("#616161"));

        tabAll.setBackgroundColor(Color.TRANSPARENT);
        tabToday.setBackgroundColor(Color.TRANSPARENT);
        tabUpcoming.setBackgroundColor(Color.TRANSPARENT);

        if (mode == FilterMode.ALL) {
            tabAll.setTextColor(Color.WHITE);
            tabAll.setBackgroundResource(R.drawable.bg_tab_selected);
        } else if (mode == FilterMode.TODAY) {
            tabToday.setTextColor(Color.WHITE);
            tabToday.setBackgroundResource(R.drawable.bg_tab_selected);
        } else if (mode == FilterMode.UPCOMING) {
            tabUpcoming.setTextColor(Color.WHITE);
            tabUpcoming.setBackgroundResource(R.drawable.bg_tab_selected);
        }

        applyFilter();
    }

    private void applyFilter() {
        displayedTasks.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());

        for (StudyPlannerModel task : allTasks) {
            String taskDate = task.getDate() != null ? task.getDate() : "";

            if (currentFilter == FilterMode.ALL) {
                displayedTasks.add(task);
            } else if (currentFilter == FilterMode.TODAY) {
                if (todayStr.equals(taskDate)) {
                    displayedTasks.add(task);
                }
            } else if (currentFilter == FilterMode.UPCOMING) {
                if (taskDate.compareTo(todayStr) > 0) {
                    displayedTasks.add(task);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (displayedTasks.isEmpty()) {
            if (currentFilter == FilterMode.TODAY) {
                tvEmpty.setText("No study tasks scheduled for today.");
            } else if (currentFilter == FilterMode.UPCOMING) {
                tvEmpty.setText("No upcoming study tasks found.");
            } else {
                tvEmpty.setText("No study tasks added yet.\nTap + to create your first task!");
            }
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onToggleComplete(StudyPlannerModel model, boolean isCompleted) {
        String newStatus = isCompleted ? "Completed" : "Pending";
        model.setStatus(newStatus);

        db.collection("study_plans")
                .document(model.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    updateProgressStats();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onEditTask(StudyPlannerModel model) {
        Intent intent = new Intent(this, AddStudyPlanActivity.class);
        intent.putExtra(AddStudyPlanActivity.EXTRA_TASK, model);
        startActivity(intent);
    }

    @Override
    public void onDeleteTask(StudyPlannerModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this study task?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("study_plans")
                            .document(model.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(StudyPlannerActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                                allTasks.remove(model);
                                updateProgressStats();
                                applyFilter();
                            })
                            .addOnFailureListener(e -> Toast.makeText(StudyPlannerActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show());
                })
                .show();
    }
}
