package com.studentcloudhub;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studentcloudhub.model.StudyPlannerModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddStudyPlanActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "extra_task";

    private EditText etSubject;
    private EditText etTopic;
    private EditText etDate;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etDescription;
    private RadioGroup rgPriority;
    private RadioButton rbLow, rbMedium, rbHigh;
    private MaterialButton btnSavePlan;
    private TextView tvHeaderTitle;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Calendar calendarDate = Calendar.getInstance();
    private StudyPlannerModel existingModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_study_plan);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        etSubject = findViewById(R.id.etSubject);
        etTopic = findViewById(R.id.etTopic);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etDescription = findViewById(R.id.etDescription);
        rgPriority = findViewById(R.id.rgPriority);
        rbLow = findViewById(R.id.rbLow);
        rbMedium = findViewById(R.id.rbMedium);
        rbHigh = findViewById(R.id.rbHigh);
        btnSavePlan = findViewById(R.id.btnSavePlan);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Check if editing existing task
        if (getIntent().hasExtra(EXTRA_TASK)) {
            existingModel = (StudyPlannerModel) getIntent().getSerializableExtra(EXTRA_TASK);
            if (existingModel != null) {
                tvHeaderTitle.setText("Edit Study Task");
                btnSavePlan.setText("Update Study Task");
                populateFields(existingModel);
            }
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        btnSavePlan.setOnClickListener(v -> saveStudyPlan());
    }

    private void populateFields(StudyPlannerModel model) {
        etSubject.setText(model.getSubject());
        etTopic.setText(model.getTopic());
        etDate.setText(model.getDate());
        etStartTime.setText(model.getStartTime());
        etEndTime.setText(model.getEndTime());
        etDescription.setText(model.getDescription());

        String priority = model.getPriority();
        if ("High".equalsIgnoreCase(priority)) {
            rbHigh.setChecked(true);
        } else if ("Low".equalsIgnoreCase(priority)) {
            rbLow.setChecked(true);
        } else {
            rbMedium.setChecked(true);
        }
    }

    private void showDatePicker() {
        int year = calendarDate.get(Calendar.YEAR);
        int month = calendarDate.get(Calendar.MONTH);
        int day = calendarDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            calendarDate.set(Calendar.YEAR, selectedYear);
            calendarDate.set(Calendar.MONTH, selectedMonth);
            calendarDate.set(Calendar.DAY_OF_MONTH, selectedDay);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDate.setText(sdf.format(calendarDate.getTime()));
        }, year, month, day);

        dialog.show();
    }

    private void showTimePicker(final EditText targetEditText) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, selectedMinute) -> {
            Calendar timeCal = Calendar.getInstance();
            timeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            timeCal.set(Calendar.MINUTE, selectedMinute);

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            targetEditText.setText(sdf.format(timeCal.getTime()));
        }, hour, minute, false);

        dialog.show();
    }

    private void saveStudyPlan() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = etSubject.getText().toString().trim();
        String topic = etTopic.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (subject.isEmpty()) {
            etSubject.setError("Subject is required");
            etSubject.requestFocus();
            return;
        }

        if (topic.isEmpty()) {
            etTopic.setError("Topic is required");
            etTopic.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime.isEmpty()) {
            Toast.makeText(this, "Please select start time", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = "Medium";
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbHigh) {
            priority = "High";
        } else if (checkedId == R.id.rbLow) {
            priority = "Low";
        }

        btnSavePlan.setEnabled(false);

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("userId", uid);
        map.put("subject", subject);
        map.put("topic", topic);
        map.put("date", date);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("priority", priority);
        map.put("description", description);

        if (existingModel != null) {
            map.put("status", existingModel.getStatus() != null ? existingModel.getStatus() : "Pending");
            map.put("createdAt", existingModel.getCreatedAt());

            db.collection("study_plans")
                    .document(existingModel.getId())
                    .set(map)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddStudyPlanActivity.this, "Study task updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSavePlan.setEnabled(true);
                        Toast.makeText(AddStudyPlanActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            map.put("status", "Pending");
            map.put("createdAt", System.currentTimeMillis());

            db.collection("study_plans")
                    .add(map)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AddStudyPlanActivity.this, "Study task saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSavePlan.setEnabled(true);
                        Toast.makeText(AddStudyPlanActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
