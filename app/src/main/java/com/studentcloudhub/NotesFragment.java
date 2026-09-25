package com.studentcloudhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotesFragment extends Fragment {

    RecyclerView list;
    ArrayList<Model> data = new ArrayList<>();
    ModelAdapter adapter;

    FirebaseFirestore db;
    String uid;

    TextView info;
    TextView myNotesTab;
    TextView sharedTab;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_notes,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View v,
            Bundle savedInstanceState) {

        super.onViewCreated(v, savedInstanceState);

        View btnBack = v.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(x -> navigateBackToHome());
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateBackToHome();
                    }
                }
        );

        // RecyclerView
        list = v.findViewById(R.id.list);

        list.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new ModelAdapter(data);
        list.setAdapter(adapter);

        // Firebase
        db = FirebaseFirestore.getInstance();

        uid = FirebaseAuth.getInstance().getUid();

        // TextViews
        info = v.findViewById(R.id.info);

        myNotesTab = v.findViewById(R.id.myNotesTab);
        sharedTab = v.findViewById(R.id.sharedTab);

        // Add button
        v.findViewById(R.id.addButton).setOnClickListener(x -> {

            Intent intent = new Intent(
                    requireContext(),
                    AddNoteActivity.class
            );

            startActivity(intent);
        });

        // My Notes tab
        myNotesTab.setOnClickListener(x -> {

            myNotesTab.setTextColor(Color.WHITE);

            sharedTab.setTextColor(Color.DKGRAY);

            myNotesTab.setBackgroundResource(
                    R.drawable.bg_tab_selected
            );

            sharedTab.setBackgroundColor(
                    Color.TRANSPARENT
            );

            load();
        });

        // Shared Notes tab
        sharedTab.setOnClickListener(x -> {

            sharedTab.setTextColor(Color.WHITE);

            myNotesTab.setTextColor(Color.DKGRAY);

            sharedTab.setBackgroundResource(
                    R.drawable.bg_tab_selected
            );

            myNotesTab.setBackgroundColor(
                    Color.TRANSPARENT
            );

            loadShared();
        });

        // Load notes
        load();
    }


    // ============================
    // LOAD MY NOTES
    // ============================

    void load() {

        if (uid == null) return;

        info.setText("My Notes");

        db.collection("notes")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(s -> {

                    data.clear();

                    for (DocumentSnapshot d : s.getDocuments()) {

                        data.add(new Model(
                                d.getId(),
                                d.getString("title"),
                                d.getString("subject"),
                                d.getString("description"),
                                d.getString("date")
                        ));
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            getContext(),
                            "Failed to load notes",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }


    // ============================
    // LOAD SHARED NOTES
    // ============================

    void loadShared() {

        if (uid == null) return;

        info.setText("Shared Notes");

        db.collection("notes")
                .whereEqualTo("shared", true)
                .get()
                .addOnSuccessListener(s -> {

                    data.clear();

                    for (DocumentSnapshot d : s.getDocuments()) {

                        data.add(new Model(
                                d.getId(),
                                d.getString("title"),
                                d.getString("subject"),
                                d.getString("description"),
                                d.getString("date")
                        ));
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            getContext(),
                            "Failed to load shared notes",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }

    private void navigateBackToHome() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).selectTab(R.id.home);
        } else if (isAdded() && getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}