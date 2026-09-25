package com.studentcloudhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText email;
    Button sendReset;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        sendReset = findViewById(R.id.sendReset);

        sendReset.setOnClickListener(v -> reset());
    }

    private void reset() {

        String userEmail =
                email.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {

            email.setError("Enter your email");
            email.requestFocus();

            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(userEmail)
                .matches()) {

            email.setError("Enter a valid email");
            email.requestFocus();

            return;
        }

        sendReset.setEnabled(false);
        sendReset.setText("Sending...");

        auth.sendPasswordResetEmail(userEmail)
                .addOnCompleteListener(task -> {

                    sendReset.setEnabled(true);
                    sendReset.setText("Send Reset Link");

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ResetPasswordActivity.this,
                                "Reset email sent. Check Gmail, Spam and Promotions.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        String error =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unknown Firebase error";

                        Toast.makeText(
                                ResetPasswordActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}