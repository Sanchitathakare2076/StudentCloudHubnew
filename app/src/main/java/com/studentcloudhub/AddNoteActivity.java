package com.studentcloudhub;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    EditText etNoteTitle;
    EditText etNoteSubject;
    EditText etNoteDescription;

    ImageView btnBack;
    MaterialButton btnSaveNote;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_note);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Views
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteSubject = findViewById(R.id.etNoteSubject);
        etNoteDescription = findViewById(R.id.etNoteDescription);

        btnBack = findViewById(R.id.btnBack);
        btnSaveNote = findViewById(R.id.btnSaveNote);


        // Back
        btnBack.setOnClickListener(v -> {

            finish();

        });


        // Save
        btnSaveNote.setOnClickListener(v -> {

            saveNote();

        });
    }


    private void saveNote() {

        String title = etNoteTitle
                .getText()
                .toString()
                .trim();

        String subject = etNoteSubject
                .getText()
                .toString()
                .trim();

        String description = etNoteDescription
                .getText()
                .toString()
                .trim();


        // Validate title
        if (title.isEmpty()) {

            etNoteTitle.setError(
                    "Title cannot be empty"
            );

            etNoteTitle.requestFocus();

            return;
        }


        // Validate description
        if (description.isEmpty()) {

            etNoteDescription.setError(
                    "Write something in your note"
            );

            etNoteDescription.requestFocus();

            return;
        }


        // Check user
        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String uid = auth
                .getCurrentUser()
                .getUid();


        // Firestore data
        Map<String, Object> m = new HashMap<>();

        m.put("userId", uid);

        m.put("title", title);

        m.put(
                "subject",
                subject.isEmpty()
                        ? "General"
                        : subject
        );

        m.put(
                "description",
                description
        );


        // Date
        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "d MMM yyyy",
                        Locale.getDefault()
                );

        m.put(
                "date",
                sdf.format(new Date())
        );


        // Shared
        m.put(
                "shared",
                false
        );


        // Created time
        m.put(
                "createdAt",
                System.currentTimeMillis()
        );


        // Disable button
        btnSaveNote.setEnabled(false);

        btnSaveNote.setText(
                "Saving..."
        );


        // Save to Firestore
        db.collection("notes")
                .add(m)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            AddNoteActivity.this,
                            "Note Added",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                })
                .addOnFailureListener(e -> {

                    btnSaveNote.setEnabled(true);

                    btnSaveNote.setText(
                            "Save Note"
                    );

                    Toast.makeText(
                            AddNoteActivity.this,
                            "Failed to save note",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }
}