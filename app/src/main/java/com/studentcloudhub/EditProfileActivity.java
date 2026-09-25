
package com.studentcloudhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etEmail, etMobile, etCollege, etBranch;
    Button btnSave;
    ImageButton btnBack;

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        // Find Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etCollege = findViewById(R.id.etCollege);
        etBranch = findViewById(R.id.etBranch);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Back Button
        btnBack.setOnClickListener(v -> finish());

        // Load Existing Profile
        loadProfile();

        // Save Changes
        btnSave.setOnClickListener(v -> saveProfile());

    }

    private void loadProfile() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        etName.setText(document.getString("name"));

                        etEmail.setText(document.getString("email"));

                        etMobile.setText(document.getString("mobile"));

                        etCollege.setText(document.getString("college"));

                        etBranch.setText(document.getString("branch"));

                    } else {

                        Toast.makeText(
                                this,
                                "Profile not found",
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                })
                .addOnFailureListener(error -> {

                    Toast.makeText(
                            this,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                    ).show();

                });

    }

    private void saveProfile() {

        String name = etName.getText().toString().trim();

        String email = etEmail.getText().toString().trim();

        String mobile = etMobile.getText().toString().trim();

        String college = etCollege.getText().toString().trim();

        String branch = etBranch.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {

            etName.setError("Enter your name");

            etName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(email)) {

            etEmail.setError("Enter email");

            etEmail.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(mobile)) {

            etMobile.setError("Enter mobile number");

            etMobile.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(college)) {

            etCollege.setError("Enter college");

            etCollege.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(branch)) {

            etBranch.setError("Enter branch");

            etBranch.requestFocus();

            return;
        }

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Prepare updated data
        Map<String, Object> profile = new HashMap<>();

        profile.put("name", name);
        profile.put("email", email);
        profile.put("mobile", mobile);
        profile.put("college", college);
        profile.put("branch", branch);

        btnSave.setEnabled(false);

        btnSave.setText("Saving...");

        // Update Firestore
        firestore.collection("users")
                .document(uid)
                .update(profile)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                })
                .addOnFailureListener(error -> {

                    btnSave.setEnabled(true);

                    btnSave.setText("Save Changes");

                    Toast.makeText(
                            this,
                            "Update failed: " + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

    }

}