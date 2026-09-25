package com.studentcloudhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

 EditText name, email, mobile, college, branch, password, confirm;
 Button action, forgot, toggle;
 ProgressBar progress;
 CheckBox show_password;

 boolean register = false;

 FirebaseAuth auth;
 FirebaseFirestore db;

 @SuppressLint("MissingInflatedId")
 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  setContentView(R.layout.activity_auth);

  auth = FirebaseAuth.getInstance();
  db = FirebaseFirestore.getInstance();

  name = findViewById(R.id.name);
  email = findViewById(R.id.email);
  mobile = findViewById(R.id.mobile);
  college = findViewById(R.id.college);
  branch = findViewById(R.id.branch);
  password = findViewById(R.id.password);
  confirm = findViewById(R.id.confirm);

  action = findViewById(R.id.action);
  forgot = findViewById(R.id.forgot);
  toggle = findViewById(R.id.toggle);
  progress = findViewById(R.id.progress);

  show_password=findViewById(R.id.show_password);
  show_password.setOnCheckedChangeListener((buttonView, isChecked) -> {

   if (isChecked) {

    password.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    );

    confirm.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    );

   } else {

    password.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    );

    confirm.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    );
   }

   password.setSelection(password.length());
   confirm.setSelection(confirm.length());
  });

  action.setOnClickListener(v -> submit());

  toggle.setOnClickListener(v -> {
   register = !register;
   updateMode();
  });

  forgot.setOnClickListener(v -> {

   Intent intent =
           new Intent(
                   AuthActivity.this,
                   ResetPasswordActivity.class
           );

   startActivity(intent);
  });
 }

 private void updateMode() {

  name.setVisibility(register ? View.VISIBLE : View.GONE);
  mobile.setVisibility(register ? View.VISIBLE : View.GONE);
  college.setVisibility(register ? View.VISIBLE : View.GONE);
  branch.setVisibility(register ? View.VISIBLE : View.GONE);
  confirm.setVisibility(register ? View.VISIBLE : View.GONE);

  action.setText(register ? "Create Account" : "Login");

  forgot.setVisibility(register ? View.GONE : View.VISIBLE);

  toggle.setText(
          register
                  ? "Already have an account"
                  : "Create New Account"
  );
 }

 private void loading(boolean value) {

  progress.setVisibility(
          value ? View.VISIBLE : View.GONE
  );

  action.setEnabled(!value);
 }

 private boolean empty(EditText editText) {

  return TextUtils.isEmpty(
          editText.getText().toString().trim()
  );
 }

 private void submit() {

  String userEmail =
          email.getText().toString().trim();

  String userPassword =
          password.getText().toString();

  if (empty(email) || empty(password)) {

   Toast.makeText(
           this,
           "Please enter email and password",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  if (register) {

   if (empty(name) ||
           empty(mobile) ||
           empty(college) ||
           empty(branch) ||
           empty(confirm)) {

    Toast.makeText(
            this,
            "Please fill all fields",
            Toast.LENGTH_SHORT
    ).show();

    return;
   }

   if (!userPassword.equals(
           confirm.getText().toString())) {

    confirm.setError(
            "Passwords do not match"
    );

    return;
   }
  }

  loading(true);

  if (!register) {

   loginUser(userEmail, userPassword);

  } else {

   registerUser(
           userEmail,
           userPassword
   );
  }
 }

 private void loginUser(
         String userEmail,
         String userPassword) {

  auth.signInWithEmailAndPassword(
          userEmail,
          userPassword
  ).addOnCompleteListener(task -> {

   loading(false);

   if (task.isSuccessful()) {

    Toast.makeText(
            this,
            "Login successful",
            Toast.LENGTH_SHORT
    ).show();

    Intent intent =
            new Intent(
                    AuthActivity.this,
                    MainActivity.class
            );

    startActivity(intent);
    finish();

   } else {

    String error =
            task.getException() != null
                    ? task.getException().getMessage()
                    : "Login failed";

    Toast.makeText(
            this,
            error,
            Toast.LENGTH_LONG
    ).show();
   }
  });
 }

 private void registerUser(
         String userEmail,
         String userPassword) {

  auth.createUserWithEmailAndPassword(
          userEmail,
          userPassword
  ).addOnCompleteListener(task -> {

   if (!task.isSuccessful()) {

    loading(false);

    String error =
            task.getException() != null
                    ? task.getException().getMessage()
                    : "Registration failed";

    Toast.makeText(
            this,
            error,
            Toast.LENGTH_LONG
    ).show();

    return;
   }

   String uid =
           auth.getCurrentUser().getUid();

   Map<String, Object> user =
           new HashMap<>();

   user.put(
           "name",
           name.getText().toString().trim()
   );

   user.put(
           "email",
           email.getText().toString().trim()
   );

   user.put(
           "mobile",
           mobile.getText().toString().trim()
   );

   user.put(
           "college",
           college.getText().toString().trim()
   );

   user.put(
           "branch",
           branch.getText().toString().trim()
   );

   user.put(
           "createdAt",
           System.currentTimeMillis()
   );

   db.collection("users")
           .document(uid)
           .set(user)
           .addOnCompleteListener(
                   firestoreTask -> {

                    loading(false);

                    if (firestoreTask.isSuccessful()) {

                     Toast.makeText(
                             this,
                             "Account created successfully",
                             Toast.LENGTH_SHORT
                     ).show();

                     Intent intent =
                             new Intent(
                                     AuthActivity.this,
                                     MainActivity.class
                             );

                     startActivity(intent);
                     finish();

                    } else {

                     Toast.makeText(
                             this,
                             "Account created, but profile could not be saved",
                             Toast.LENGTH_LONG
                     ).show();
                    }
                   }
           );
  });
 }

 private void reset() {

  String userEmail =
          email.getText().toString().trim();

  if (userEmail.isEmpty()) {

   email.setError(
           "Enter your email"
   );

   return;
  }

  auth.sendPasswordResetEmail(
          userEmail
  ).addOnCompleteListener(task -> {

   if (task.isSuccessful()) {

    Toast.makeText(
            this,
            "Password reset email sent",
            Toast.LENGTH_LONG
    ).show();

   } else {

    Toast.makeText(
            this,
            "Unable to send reset email",
            Toast.LENGTH_LONG
    ).show();
   }
  });
 }
}