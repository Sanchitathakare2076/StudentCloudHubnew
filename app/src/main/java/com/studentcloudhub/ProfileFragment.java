package com.studentcloudhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

 private TextView header;
 private TextView info;
 private TextView editProfile;

 private FirebaseAuth firebaseAuth;
 private FirebaseFirestore firestore;

 public ProfileFragment() {
  // Required empty constructor
 }

 @Nullable
 @Override
 public View onCreateView(
         @NonNull LayoutInflater inflater,
         @Nullable ViewGroup container,
         @Nullable Bundle savedInstanceState) {

  View view = inflater.inflate(
          R.layout.fragment_profile,
          container,
          false
  );

  header = view.findViewById(R.id.header);
  info = view.findViewById(R.id.info);
  editProfile = view.findViewById(R.id.editprofile);

  firebaseAuth = FirebaseAuth.getInstance();
  firestore = FirebaseFirestore.getInstance();
  LinearLayout rowLogout = view.findViewById(R.id.rowLogout);

  rowLogout.setOnClickListener(v -> {

   new AlertDialog.Builder(requireContext())
           .setTitle("Logout")
           .setMessage("Are you sure you want to logout?")
           .setNegativeButton("Cancel", null)
           .setPositiveButton("Logout", (dialog, which) -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(
                    requireContext(),
                    AuthActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
           })
           .show();

  });
  LinearLayout rowDeleteAccount =
          view.findViewById(R.id.rowDeleteAccount);

  rowDeleteAccount.setOnClickListener(v -> {

   new AlertDialog.Builder(requireContext())
           .setTitle("Delete Account")
           .setMessage(
                   "Are you sure you want to delete your account?\n\n" +
                           "All your profile data will be permanently deleted."
           )
           .setNegativeButton("Cancel", null)
           .setPositiveButton("Delete", (dialog, which) -> {

            deleteAccount();

           })
           .show();

  });

  // Open Edit Profile page
  editProfile.setOnClickListener(v -> {
   Intent intent = new Intent(
           requireContext(),
           EditProfileActivity.class
   );

   startActivity(intent);
  });

  loadProfileData();

  return view;
 }
 private void deleteAccount() {

  FirebaseAuth auth = FirebaseAuth.getInstance();
  FirebaseFirestore firestore = FirebaseFirestore.getInstance();

  if (auth.getCurrentUser() == null) {

   Toast.makeText(
           requireContext(),
           "User not logged in",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  String uid = auth.getCurrentUser().getUid();

  firestore.collection("users")
          .document(uid)
          .delete()
          .addOnSuccessListener(unused -> {

           auth.getCurrentUser()
                   .delete()
                   .addOnSuccessListener(unusedAuth -> {

                    Toast.makeText(
                            requireContext(),
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            requireContext(),
                            AuthActivity.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);

                   })
                   .addOnFailureListener(error -> {

                    Toast.makeText(
                            requireContext(),
                            "Authentication deletion failed: "
                                    + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                   });

          })
          .addOnFailureListener(error -> {

           Toast.makeText(
                   requireContext(),
                   "Database deletion failed: "
                           + error.getMessage(),
                   Toast.LENGTH_LONG
           ).show();

          });
 }
 private void loadProfileData() {

  if (firebaseAuth.getCurrentUser() == null) {
   Toast.makeText(
           requireContext(),
           "User is not logged in",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  String userId = firebaseAuth
          .getCurrentUser()
          .getUid();

  firestore.collection("users")
          .document(userId)
          .get()
          .addOnSuccessListener(documentSnapshot -> {

           if (documentSnapshot.exists()) {

            String name = documentSnapshot
                    .getString("name");

            String email = documentSnapshot
                    .getString("email");

            if (name != null && !name.isEmpty()) {
             header.setText(name);
            } else {
             header.setText("Student");
            }

            if (email != null && !email.isEmpty()) {
             info.setText(email);
            } else {
             info.setText(
                     firebaseAuth
                             .getCurrentUser()
                             .getEmail()
             );
            }

           } else {

            header.setText("Student");

            String email = firebaseAuth
                    .getCurrentUser()
                    .getEmail();

            if (email != null) {
             info.setText(email);
            } else {
             info.setText("Email not available");
            }
           }
          })
          .addOnFailureListener(e -> {

           Toast.makeText(
                   requireContext(),
                   "Failed to load profile",
                   Toast.LENGTH_SHORT
           ).show();
          });
 }

 @Override
 public void onResume() {
  super.onResume();

  if (firebaseAuth != null) {
   loadProfileData();
  }
 }
}