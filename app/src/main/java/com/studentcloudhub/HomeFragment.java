package com.studentcloudhub;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

 private TextView tvGreeting;
 private TextView tvPlanLoading;
 private TextView tvMath;
 private TextView tvScience;

 private LinearLayout rowMath;
 private LinearLayout rowScience,cardAiTutor;

 private FirebaseAuth firebaseAuth;
 private FirebaseFirestore firestore;

 @Nullable
 @Override
 public View onCreateView(
         @NonNull LayoutInflater inflater,
         ViewGroup container,
         Bundle savedInstanceState) {

  return inflater.inflate(
          R.layout.fragment_home,
          container,
          false
  );
 }

 @Override
 public void onViewCreated(
         @NonNull View view,
         Bundle savedInstanceState) {

  super.onViewCreated(view, savedInstanceState);

  tvGreeting = view.findViewById(R.id.tvGreeting);
  tvPlanLoading = view.findViewById(R.id.tvPlanLoading);
  tvMath = view.findViewById(R.id.tvMath);
  tvScience = view.findViewById(R.id.tvScience);
  rowMath = view.findViewById(R.id.rowMath);
  rowScience = view.findViewById(R.id.rowScience);

  firebaseAuth = FirebaseAuth.getInstance();
  firestore = FirebaseFirestore.getInstance();

  loadStudentName();
  loadTodayPlan();

  View aiTutorCard = view.findViewById(R.id.cardAiTutor);

  aiTutorCard.setOnClickListener(v -> {

   Intent intent = new Intent(
           requireContext(),
           AIActivity.class
   );

   startActivity(intent);
  });

  View subjectsCard = view.findViewById(R.id.cardSubjects);
  subjectsCard.setOnClickListener(v -> {
   if (getActivity() instanceof MainActivity) {
    ((MainActivity) getActivity()).selectTab(R.id.subjects);
   }
  });

  View notesCard = view.findViewById(R.id.cardNotes);
  notesCard.setOnClickListener(v -> {
   if (getActivity() instanceof MainActivity) {
    ((MainActivity) getActivity()).selectTab(R.id.notes);
   }
  });

  View plannerCard = view.findViewById(R.id.cardPlanner);
  plannerCard.setOnClickListener(v -> {
   Intent intent = new Intent(requireContext(), StudyPlannerActivity.class);
   startActivity(intent);
  });

  View testsCard = view.findViewById(R.id.cardTests);
  testsCard.setOnClickListener(v -> {
   Intent intent = new Intent(requireContext(), TestHomeActivity.class);
   startActivity(intent);
  });

  View viewAll = view.findViewById(R.id.tvViewAll);
  if (viewAll != null) {
   viewAll.setOnClickListener(v -> {
    Intent intent = new Intent(requireContext(), StudyPlannerActivity.class);
    startActivity(intent);
   });
  }

  View moreCard = view.findViewById(R.id.cardMore);

  moreCard.setOnClickListener(v -> {

   Toast.makeText(
           requireContext(),
           "More clicked",
           Toast.LENGTH_SHORT
   ).show();
  });
 }

 private void loadStudentName() {

  if (firebaseAuth.getCurrentUser() == null) {

   tvGreeting.setText("Hello, Student 👋");

   return;
  }

  String uid = firebaseAuth.getCurrentUser().getUid();

  firestore.collection("users")
          .document(uid)
          .get()
          .addOnSuccessListener(document -> {

           String name = document.getString("name");

           if (name == null || name.trim().isEmpty()) {
            name = "Student";
           }

           tvGreeting.setText(
                   "Hello, " + name + " 👋"
           );
          })
          .addOnFailureListener(error -> {

           tvGreeting.setText("Hello, Student 👋");
          });
 }

 private void loadTodayPlan() {

  if (firebaseAuth.getCurrentUser() == null) {
   tvPlanLoading.setText("Please login to view today's plan");
   return;
  }

  String uid = firebaseAuth.getCurrentUser().getUid();
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
  String todayStr = sdf.format(new Date());

  firestore.collection("study_plans")
          .whereEqualTo("userId", uid)
          .whereEqualTo("date", todayStr)
          .get()
          .addOnSuccessListener(querySnapshot -> {
           if (querySnapshot == null || querySnapshot.isEmpty()) {
            loadLegacyTodayPlan(uid);
            return;
           }

           tvPlanLoading.setVisibility(View.GONE);
           rowMath.setVisibility(View.GONE);
           rowScience.setVisibility(View.GONE);

           List<DocumentSnapshot> docs = querySnapshot.getDocuments();

           if (docs.size() > 0) {
            DocumentSnapshot d1 = docs.get(0);
            String sub1 = d1.getString("subject");
            String top1 = d1.getString("topic");
            String time1 = d1.getString("startTime");
            tvMath.setText((sub1 != null ? sub1 : "Task") + ": " + (top1 != null ? top1 : "") + (time1 != null ? " (" + time1 + ")" : ""));
            rowMath.setVisibility(View.VISIBLE);
           }

           if (docs.size() > 1) {
            DocumentSnapshot d2 = docs.get(1);
            String sub2 = d2.getString("subject");
            String top2 = d2.getString("topic");
            String time2 = d2.getString("startTime");
            tvScience.setText((sub2 != null ? sub2 : "Task") + ": " + (top2 != null ? top2 : "") + (time2 != null ? " (" + time2 + ")" : ""));
            rowScience.setVisibility(View.VISIBLE);
           }
          })
          .addOnFailureListener(error -> loadLegacyTodayPlan(uid));
 }

 private void loadLegacyTodayPlan(String uid) {
  firestore.collection("users")
          .document(uid)
          .collection("todayPlan")
          .document("plan")
          .get()
          .addOnSuccessListener(document -> {
           if (!document.exists()) {
            tvPlanLoading.setText("No study plan added for today");
            tvPlanLoading.setVisibility(View.VISIBLE);
            rowMath.setVisibility(View.GONE);
            rowScience.setVisibility(View.GONE);
            return;
           }

           String math = document.getString("math");
           String science = document.getString("science");

           if ((math == null || math.trim().isEmpty()) && (science == null || science.trim().isEmpty())) {
            tvPlanLoading.setText("No study plan added for today");
            tvPlanLoading.setVisibility(View.VISIBLE);
            rowMath.setVisibility(View.GONE);
            rowScience.setVisibility(View.GONE);
            return;
           }

           tvPlanLoading.setVisibility(View.GONE);

           if (math != null && !math.trim().isEmpty()) {
            tvMath.setText(math);
            rowMath.setVisibility(View.VISIBLE);
           }

           if (science != null && !science.trim().isEmpty()) {
            tvScience.setText(science);
            rowScience.setVisibility(View.VISIBLE);
           }
          })
          .addOnFailureListener(error -> {
           tvPlanLoading.setText("No study plan added for today");
           tvPlanLoading.setVisibility(View.VISIBLE);
          });
 }

 @Override
 public void onResume() {
  super.onResume();
  if (firebaseAuth != null) {
   loadTodayPlan();
  }
 }
}