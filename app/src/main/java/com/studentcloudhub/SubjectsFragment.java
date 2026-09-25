package com.studentcloudhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectsFragment extends Fragment {

    private static final String XAMPP_DELETE_URL =
            "http://10.61.149.231/studentcloudhub/delete_pdf.php";

    private TextView header;
    private TextView info;
    private FloatingActionButton addButton;
    private RecyclerView list;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private SubjectAdapter adapter;
    private ArrayList<Subject> subjectList;

    private String selectedSubjectName = "";

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    // ============================================================
    // PDF PICKER
    // ============================================================

    private final ActivityResultLauncher<String> pdfPicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {
                            uploadPdf(uri);
                        }
                    }
            );

    // ============================================================
    // ON CREATE VIEW
    // ============================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_subjects,
                container,
                false
        );

        header = view.findViewById(R.id.header);
        info = view.findViewById(R.id.info);
        addButton = view.findViewById(R.id.addButton);
        list = view.findViewById(R.id.list);

        // --------------------------------------------------------
        // BACK BUTTON
        // --------------------------------------------------------

        View btnBack = view.findViewById(R.id.btnBack);

        if (btnBack != null) {

            btnBack.setOnClickListener(
                    v -> navigateBackToHome()
            );
        }

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                navigateBackToHome();
                            }
                        }
                );

        // --------------------------------------------------------
        // FIREBASE
        // --------------------------------------------------------

        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();

        // --------------------------------------------------------
        // LIST
        // --------------------------------------------------------

        subjectList = new ArrayList<>();

        adapter = new SubjectAdapter(subjectList);

        list.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        list.setAdapter(adapter);

        // --------------------------------------------------------
        // ADD BUTTON
        // --------------------------------------------------------

        addButton.setOnClickListener(
                v -> showAddSubjectDialog()
        );

        // --------------------------------------------------------
        // LOAD DATA
        // --------------------------------------------------------

        loadSubjects();

        return view;
    }

    // ============================================================
    // BACK TO HOME
    // ============================================================

    private void navigateBackToHome() {

        if (getActivity() instanceof MainActivity) {

            ((MainActivity) getActivity())
                    .selectTab(R.id.home);

        } else if (
                isAdded()
                        && getParentFragmentManager()
                        .getBackStackEntryCount() > 0
        ) {

            getParentFragmentManager()
                    .popBackStack();
        }
    }

    // ============================================================
    // LOAD SUBJECTS
    // ============================================================

    private void loadSubjects() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            info.setText("Please login first");

            return;
        }

        String uid = currentUser.getUid();

        info.setText("Loading subjects...");

        db.collection("users")
                .document(uid)
                .collection("subjects")
                .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    subjectList.clear();

                    for (
                            DocumentSnapshot document :
                            querySnapshot.getDocuments()
                    ) {

                        Subject subject = new Subject();

                        subject.id = document.getId();

                        String subjectName =
                                document.getString("subjectName");

                        String pdfName =
                                document.getString("pdfName");

                        String serverFileName =
                                document.getString("serverFileName");

                        String pdfUrl =
                                document.getString("pdfUrl");

                        Long createdAt =
                                document.getLong("createdAt");

                        subject.subjectName =
                                subjectName != null
                                        ? subjectName
                                        : "Subject";

                        subject.pdfName =
                                pdfName != null
                                        ? pdfName
                                        : "Study Material.pdf";

                        subject.pdfUrl =
                                pdfUrl != null
                                        ? pdfUrl
                                        : "";

                        subject.createdAt =
                                createdAt != null
                                        ? createdAt
                                        : 0;

                        // ------------------------------------------------
                        // IMPORTANT:
                        // For older records serverFileName may not exist.
                        // Extract filename from URL as fallback.
                        // ------------------------------------------------

                        if (
                                serverFileName != null
                                        && !serverFileName.trim().isEmpty()
                        ) {

                            subject.serverFileName =
                                    serverFileName;

                        } else {

                            subject.serverFileName =
                                    getFileNameFromUrl(pdfUrl);
                        }

                        subjectList.add(subject);
                    }

                    adapter.notifyDataSetChanged();

                    if (subjectList.isEmpty()) {

                        info.setText(
                                "No subjects added yet"
                        );

                    } else {

                        info.setText(
                                subjectList.size()
                                        + " subjects available"
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    info.setText("Could not load subjects");

                    Toast.makeText(
                            requireContext(),
                            "ERROR: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ============================================================
    // ADD SUBJECT DIALOG
    // ============================================================

    private void showAddSubjectDialog() {

        final EditText subjectInput =
                new EditText(requireContext());

        subjectInput.setHint(
                "Enter subject name"
        );

        subjectInput.setSingleLine(true);

        subjectInput.setPadding(
                30,
                20,
                30,
                20
        );

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Add Subject")
                        .setMessage(
                                "Enter subject name and select a PDF"
                        )
                        .setView(subjectInput)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Select PDF",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String subjectName =
                        subjectInput
                                .getText()
                                .toString()
                                .trim();

                if (subjectName.isEmpty()) {

                    subjectInput.setError(
                            "Enter subject name"
                    );

                    return;
                }

                selectedSubjectName =
                        subjectName;

                dialog.dismiss();

                openPdfPicker();
            });
        });

        dialog.show();
    }

    // ============================================================
    // OPEN PDF PICKER
    // ============================================================

    private void openPdfPicker() {

        pdfPicker.launch(
                "application/pdf"
        );
    }

    // ============================================================
    // UPLOAD PDF
    // ============================================================

    private void uploadPdf(Uri pdfUri) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final String uid =
                user.getUid();

        final String finalSubjectName =
                selectedSubjectName;

        if (
                finalSubjectName == null
                        || finalSubjectName.trim().isEmpty()
        ) {

            Toast.makeText(
                    requireContext(),
                    "Please select a subject",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String fileName =
                getFileName(pdfUri);

        if (
                fileName == null
                        || fileName.trim().isEmpty()
        ) {

            fileName = "document.pdf";
        }

        final String finalPdfName =
                fileName;

        final String storageFileName =
                uid
                        + "_"
                        + System.currentTimeMillis()
                        + "_"
                        + finalPdfName;

        info.setText(
                "Uploading PDF to XAMPP..."
        );

        addButton.setEnabled(false);

        // ========================================================
        // UPLOAD TO XAMPP
        // ========================================================

        XamppUploader.uploadPdfToXampp(
                requireContext(),
                pdfUri,
                storageFileName,
                XamppUploader.DEFAULT_XAMPP_URL,

                new XamppUploader.UploadCallback() {

                    @Override
                    public void onSuccess(
                            String xamppPdfUrl
                    ) {

                        requireActivity()
                                .runOnUiThread(() -> {

                                    if (
                                            xamppPdfUrl == null
                                                    || xamppPdfUrl
                                                    .trim()
                                                    .isEmpty()
                                    ) {

                                        addButton.setEnabled(true);

                                        Toast.makeText(
                                                requireContext(),
                                                "PDF uploaded, but URL is empty",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    // ------------------------------------
                                    // GET ACTUAL SERVER FILE NAME
                                    // ------------------------------------

                                    String serverFileName =
                                            getFileNameFromUrl(
                                                    xamppPdfUrl
                                            );

                                    if (
                                            serverFileName == null
                                                    || serverFileName
                                                    .trim()
                                                    .isEmpty()
                                    ) {

                                        addButton.setEnabled(true);

                                        Toast.makeText(
                                                requireContext(),
                                                "Could not get server filename",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    Toast.makeText(
                                            requireContext(),
                                            "PDF uploaded successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    info.setText(
                                            "Saving PDF information..."
                                    );

                                    // ------------------------------------
                                    // SAVE FIRESTORE
                                    // ------------------------------------

                                    saveSubjectToFirestore(
                                            uid,
                                            finalSubjectName,
                                            finalPdfName,
                                            serverFileName,
                                            xamppPdfUrl
                                    );
                                });
                    }

                    @Override
                    public void onError(
                            String errorMessage
                    ) {

                        requireActivity()
                                .runOnUiThread(() -> {

                                    addButton.setEnabled(true);

                                    info.setText(
                                            "Upload failed"
                                    );

                                    Toast.makeText(
                                            requireContext(),
                                            errorMessage,
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    }
                }
        );
    }

    // ============================================================
    // SAVE SUBJECT TO FIRESTORE
    // ============================================================

    private void saveSubjectToFirestore(
            String uid,
            String subjectName,
            String pdfName,
            String serverFileName,
            String pdfUrl
    ) {

        Map<String, Object> subject =
                new HashMap<>();

        subject.put(
                "subjectName",
                subjectName
        );

        // Original filename shown to user
        subject.put(
                "pdfName",
                pdfName
        );

        // Actual filename inside XAMPP /pdfs/
        subject.put(
                "serverFileName",
                serverFileName
        );

        // URL to open PDF
        subject.put(
                "pdfUrl",
                pdfUrl
        );

        subject.put(
                "createdAt",
                System.currentTimeMillis()
        );

        db.collection("users")
                .document(uid)
                .collection("subjects")
                .add(subject)
                .addOnSuccessListener(
                        documentReference -> {

                            addButton.setEnabled(true);

                            selectedSubjectName = "";

                            Toast.makeText(
                                    requireContext(),
                                    "Subject added successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadSubjects();
                        }
                )
                .addOnFailureListener(e -> {

                    addButton.setEnabled(true);

                    info.setText(
                            "Could not save subject"
                    );

                    Toast.makeText(
                            requireContext(),
                            "Firestore save failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ============================================================
    // SHOW DELETE CONFIRMATION
    // ============================================================

    private void showDeleteConfirmation(
            Subject subject
    ) {

        new AlertDialog.Builder(requireContext())

                .setTitle("Delete PDF?")

                .setMessage(
                        "Are you sure you want to delete \""
                                + subject.pdfName
                                + "\"?\n\n"
                                + "The PDF will be removed from XAMPP and "
                                + "its Firestore record will also be deleted."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            deleteSubject(subject);
                        }
                )

                .show();
    }

    // ============================================================
    // DELETE SUBJECT
    // ============================================================

    private void deleteSubject(
            Subject subject
    ) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    requireContext(),
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (
                subject.serverFileName == null
                        || subject.serverFileName
                        .trim()
                        .isEmpty()
        ) {

            Toast.makeText(
                    requireContext(),
                    "Server filename not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        addButton.setEnabled(false);

        info.setText(
                "Deleting PDF..."
        );

        // ========================================================
        // FIRST DELETE ACTUAL PDF FROM XAMPP
        // ========================================================

        deletePdfFromXampp(
                subject.serverFileName,

                new DeleteCallback() {

                    @Override
                    public void onSuccess(
                            String message
                    ) {

                        requireActivity()
                                .runOnUiThread(() -> {

                                    info.setText(
                                            "Deleting Firestore record..."
                                    );

                                    // --------------------------------
                                    // THEN DELETE FIRESTORE DOCUMENT
                                    // --------------------------------

                                    deleteFirestoreSubject(
                                            subject
                                    );
                                });
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        requireActivity()
                                .runOnUiThread(() -> {

                                    addButton.setEnabled(true);

                                    info.setText(
                                            "Delete failed"
                                    );

                                    Toast.makeText(
                                            requireContext(),
                                            "XAMPP Delete Failed:\n"
                                                    + error,
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    }
                }
        );
    }

    // ============================================================
    // DELETE PDF FROM XAMPP
    // ============================================================

    private void deletePdfFromXampp(
            String fileName,
            DeleteCallback callback
    ) {

        executorService.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(
                                XAMPP_DELETE_URL
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

                connection.setDoOutput(true);

                connection.setDoInput(true);

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        15000
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                );

                // ------------------------------------------------
                // SEND ACTUAL SERVER FILE NAME
                // ------------------------------------------------

                String postData =
                        "file_name="
                                + URLEncoder.encode(
                                fileName,
                                "UTF-8"
                        );

                OutputStream outputStream =
                        connection.getOutputStream();

                outputStream.write(
                        postData.getBytes(
                                "UTF-8"
                        )
                );

                outputStream.flush();

                outputStream.close();

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200
                        && responseCode < 400) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }

                StringBuilder response =
                        new StringBuilder();

                if (inputStream != null) {

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            inputStream
                                    )
                            );

                    String line;

                    while (
                            (line = reader.readLine())
                                    != null
                    ) {

                        response.append(line);
                    }

                    reader.close();
                }

                String responseText =
                        response.toString();

                // ------------------------------------------------
                // CHECK PHP RESPONSE
                // ------------------------------------------------

                if (
                        responseCode >= 200
                                && responseCode < 400
                ) {

                    if (
                            responseText.contains(
                                    "\"success\":true"
                            )
                    ) {

                        callback.onSuccess(
                                responseText
                        );

                    } else {

                        callback.onError(
                                responseText
                        );
                    }

                } else {

                    callback.onError(
                            "HTTP Error "
                                    + responseCode
                                    + "\n"
                                    + responseText
                    );
                }

            } catch (Exception e) {

                callback.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Connection error"
                );

            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }
        });
    }

    // ============================================================
    // DELETE FIRESTORE DOCUMENT
    // ============================================================

    private void deleteFirestoreSubject(
            Subject subject
    ) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            addButton.setEnabled(true);

            return;
        }

        String uid =
                user.getUid();

        db.collection("users")
                .document(uid)
                .collection("subjects")
                .document(subject.id)
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            addButton.setEnabled(true);

                            Toast.makeText(
                                    requireContext(),
                                    "PDF deleted successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadSubjects();
                        }
                )
                .addOnFailureListener(e -> {

                    addButton.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "PDF removed from XAMPP, "
                                    + "but Firestore delete failed:\n"
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    loadSubjects();
                });
    }

    // ============================================================
    // DELETE CALLBACK
    // ============================================================

    private interface DeleteCallback {

        void onSuccess(String message);

        void onError(String error);
    }

    // ============================================================
    // GET FILE NAME FROM URI
    // ============================================================

    private String getFileName(Uri uri) {

        String result = null;

        if (
                "content".equals(
                        uri.getScheme()
                )
        ) {

            Cursor cursor = null;

            try {

                cursor =
                        requireContext()
                                .getContentResolver()
                                .query(
                                        uri,
                                        null,
                                        null,
                                        null,
                                        null
                                );

                if (
                        cursor != null
                                && cursor.moveToFirst()
                ) {

                    int nameIndex =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );

                    if (nameIndex >= 0) {

                        result =
                                cursor.getString(
                                        nameIndex
                                );
                    }
                }

            } finally {

                if (cursor != null) {

                    cursor.close();
                }
            }
        }

        if (result == null) {

            result =
                    uri.getLastPathSegment();
        }

        return result;
    }

    // ============================================================
    // GET FILE NAME FROM URL
    // ============================================================

    private String getFileNameFromUrl(
            String url
    ) {

        if (
                url == null
                        || url.trim().isEmpty()
        ) {

            return "";
        }

        try {

            Uri uri =
                    Uri.parse(url);

            String lastPath =
                    uri.getLastPathSegment();

            if (
                    lastPath != null
                            && !lastPath.trim().isEmpty()
            ) {

                return lastPath;
            }

        } catch (Exception ignored) {

        }

        return "";
    }

    // ============================================================
    // SUBJECT MODEL
    // ============================================================

    private static class Subject {

        String id;

        String subjectName;

        String pdfName;

        String serverFileName;

        String pdfUrl;

        long createdAt;
    }

    // ============================================================
    // RECYCLER VIEW ADAPTER
    // ============================================================

    private class SubjectAdapter
            extends RecyclerView.Adapter<
            SubjectAdapter.SubjectViewHolder> {

        private final ArrayList<Subject> subjects;

        SubjectAdapter(
                ArrayList<Subject> subjects
        ) {

            this.subjects =
                    subjects;
        }

        @NonNull
        @Override
        public SubjectViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {

            // ----------------------------------------------------
            // MAIN CARD
            // ----------------------------------------------------

            LinearLayout card =
                    new LinearLayout(
                            requireContext()
                    );

            card.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            card.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            card.setPadding(
                    20,
                    18,
                    12,
                    18
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setColor(
                    Color.WHITE
            );

            background.setCornerRadius(
                    24
            );

            card.setBackground(
                    background
            );

            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(
                    0,
                    0,
                    0,
                    14
            );

            card.setLayoutParams(
                    params
            );

            // ----------------------------------------------------
            // BOOK ICON
            // ----------------------------------------------------

            TextView icon =
                    new TextView(
                            requireContext()
                    );

            icon.setText(
                    "📚"
            );

            icon.setTextSize(
                    26
            );

            icon.setGravity(
                    Gravity.CENTER
            );

            LinearLayout.LayoutParams iconParams =
                    new LinearLayout.LayoutParams(
                            55,
                            55
                    );

            card.addView(
                    icon,
                    iconParams
            );

            // ----------------------------------------------------
            // TEXT AREA
            // ----------------------------------------------------

            LinearLayout textLayout =
                    new LinearLayout(
                            requireContext()
                    );

            textLayout.setOrientation(
                    LinearLayout.VERTICAL
            );

            textLayout.setPadding(
                    14,
                    0,
                    8,
                    0
            );

            TextView subjectName =
                    new TextView(
                            requireContext()
                    );

            subjectName.setTextSize(
                    16
            );

            subjectName.setTextColor(
                    Color.rgb(
                            30,
                            30,
                            80
                    )
            );

            subjectName.setTypeface(
                    null,
                    Typeface.BOLD
            );

            TextView pdfName =
                    new TextView(
                            requireContext()
                    );

            pdfName.setTextSize(
                    13
            );

            pdfName.setTextColor(
                    Color.rgb(
                            120,
                            120,
                            150
                    )
            );

            pdfName.setPadding(
                    0,
                    5,
                    0,
                    0
            );

            textLayout.addView(
                    subjectName
            );

            textLayout.addView(
                    pdfName
            );

            LinearLayout.LayoutParams textParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    );

            card.addView(
                    textLayout,
                    textParams
            );

            // ----------------------------------------------------
            // DELETE BUTTON
            // ----------------------------------------------------

            TextView deleteButton =
                    new TextView(
                            requireContext()
                    );

            deleteButton.setText(
                    "🗑"
            );

            deleteButton.setTextSize(
                    21
            );

            deleteButton.setGravity(
                    Gravity.CENTER
            );

            deleteButton.setPadding(
                    5,
                    5,
                    5,
                    5
            );

            LinearLayout.LayoutParams deleteParams =
                    new LinearLayout.LayoutParams(
                            50,
                            55
                    );

            card.addView(
                    deleteButton,
                    deleteParams
            );

            // ----------------------------------------------------
            // ARROW
            // ----------------------------------------------------

            TextView arrow =
                    new TextView(
                            requireContext()
                    );

            arrow.setText(
                    "›"
            );

            arrow.setTextSize(
                    28
            );

            arrow.setTextColor(
                    Color.rgb(
                            90,
                            50,
                            220
                    )
            );

            arrow.setGravity(
                    Gravity.CENTER
            );

            card.addView(
                    arrow,
                    new LinearLayout.LayoutParams(
                            35,
                            55
                    )
            );

            return new SubjectViewHolder(
                    card,
                    subjectName,
                    pdfName,
                    deleteButton
            );
        }

        @Override
        public void onBindViewHolder(
                @NonNull SubjectViewHolder holder,
                int position
        ) {

            Subject subject =
                    subjects.get(position);

            holder.subjectName.setText(
                    subject.subjectName
            );

            holder.pdfName.setText(
                    "📄 " + subject.pdfName
            );

            // ----------------------------------------------------
            // OPEN PDF
            // ----------------------------------------------------

            holder.itemView.setOnClickListener(
                    v -> openPdf(
                            subject.pdfUrl
                    )
            );

            // ----------------------------------------------------
            // DELETE PDF
            // ----------------------------------------------------

            holder.deleteButton.setOnClickListener(
                    v -> {

                        showDeleteConfirmation(
                                subject
                        );
                    }
            );
        }

        @Override
        public int getItemCount() {

            return subjects.size();
        }

        // ========================================================
        // VIEW HOLDER
        // ========================================================

        class SubjectViewHolder
                extends RecyclerView.ViewHolder {

            TextView subjectName;

            TextView pdfName;

            TextView deleteButton;

            SubjectViewHolder(
                    @NonNull View itemView,
                    TextView subjectName,
                    TextView pdfName,
                    TextView deleteButton
            ) {

                super(itemView);

                this.subjectName =
                        subjectName;

                this.pdfName =
                        pdfName;

                this.deleteButton =
                        deleteButton;
            }
        }
    }

    // ============================================================
    // OPEN PDF
    // ============================================================

    private void openPdf(
            String pdfUrl
    ) {

        if (
                pdfUrl == null
                        || pdfUrl.trim().isEmpty()
        ) {

            Toast.makeText(
                    requireContext(),
                    "PDF not available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(pdfUrl)
                );

        try {

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    requireContext(),
                    "No PDF viewer found",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (executorService != null) {

            executorService.shutdownNow();
        }
    }
}