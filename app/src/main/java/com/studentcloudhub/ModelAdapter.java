package com.studentcloudhub;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.VH> {

    private final List<Model> data;

    public ModelAdapter(List<Model> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);

        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        Model model = data.get(position);

        holder.noteTitle.setText(
                model.title != null ? model.title : "Untitled Note"
        );

        holder.noteDescription.setText(
                model.description != null ? model.description : ""
        );

        holder.noteDate.setText(
                model.date != null ? model.date : "No date"
        );

        holder.noteSubtitle.setText(
                model.subtitle != null ? model.subtitle : "General"
        );

        holder.moreButton.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(v.getContext(), holder.moreButton);

            popupMenu.getMenu().add("Edit");
            popupMenu.getMenu().add("Delete");

            popupMenu.setOnMenuItemClickListener(item -> {

                CharSequence title = item.getTitle();

                // EDIT
                if (title != null && "Edit".contentEquals(title)) {

                    View dialogView = LayoutInflater
                            .from(v.getContext())
                            .inflate(R.layout.activity_add_note, null);

                    EditText etTitle =
                            dialogView.findViewById(R.id.etNoteTitle);

                    EditText etSubject =
                            dialogView.findViewById(R.id.etNoteSubject);

                    EditText etDescription =
                            dialogView.findViewById(R.id.etNoteDescription);

                    if (etTitle != null) {
                        etTitle.setText(model.title);
                    }

                    if (etSubject != null) {
                        etSubject.setText(model.subtitle);
                    }

                    if (etDescription != null) {
                        etDescription.setText(model.description);
                    }

                    new AlertDialog.Builder(v.getContext())
                            .setView(dialogView)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Update", (dialog, which) -> {

                                String titleText =
                                        etTitle != null
                                                ? etTitle.getText().toString().trim()
                                                : "";

                                String subjectText =
                                        etSubject != null
                                                ? etSubject.getText().toString().trim()
                                                : "";

                                String descText =
                                        etDescription != null
                                                ? etDescription.getText().toString().trim()
                                                : "";

                                if (titleText.isEmpty()) {

                                    Toast.makeText(
                                            v.getContext(),
                                            "Title cannot be empty",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if (model.id != null) {

                                    Map<String, Object> updates =
                                            new HashMap<>();

                                    updates.put("title", titleText);

                                    updates.put(
                                            "subject",
                                            subjectText.isEmpty()
                                                    ? "General"
                                                    : subjectText
                                    );

                                    updates.put(
                                            "description",
                                            descText
                                    );

                                    FirebaseFirestore.getInstance()
                                            .collection("notes")
                                            .document(model.id)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid -> {

                                                Toast.makeText(
                                                        v.getContext(),
                                                        "Note updated successfully",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                model.title = titleText;

                                                model.subtitle =
                                                        subjectText.isEmpty()
                                                                ? "General"
                                                                : subjectText;

                                                model.description = descText;

                                                int currentPos =
                                                        holder.getBindingAdapterPosition();

                                                if (currentPos != RecyclerView.NO_POSITION) {
                                                    notifyItemChanged(currentPos);
                                                }
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            v.getContext(),
                                                            "Failed to update note",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                }
                            })
                            .show();

                    return true;
                }

                // DELETE
                else if (title != null && "Delete".contentEquals(title)) {

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Note")
                            .setMessage(
                                    "Are you sure you want to delete this note?"
                            )
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (dialog, which) -> {

                                if (model.id != null) {

                                    FirebaseFirestore.getInstance()
                                            .collection("notes")
                                            .document(model.id)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {

                                                Toast.makeText(
                                                        v.getContext(),
                                                        "Note deleted successfully",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                int currentPos =
                                                        holder.getBindingAdapterPosition();

                                                if (currentPos != RecyclerView.NO_POSITION
                                                        && currentPos < data.size()) {

                                                    data.remove(currentPos);

                                                    notifyItemRemoved(currentPos);
                                                }
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            v.getContext(),
                                                            "Failed to delete note",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                }
                            })
                            .show();

                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class VH extends RecyclerView.ViewHolder {

        ImageView noteIcon;
        TextView noteTitle;
        TextView noteSubtitle;
        TextView noteDescription;
        TextView noteDate;
        ImageButton moreButton;

        public VH(@NonNull View itemView) {
            super(itemView);

            noteIcon = itemView.findViewById(R.id.noteIcon);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteSubtitle = itemView.findViewById(R.id.noteSubtitle);
            noteDescription = itemView.findViewById(R.id.noteDescription);
            noteDate = itemView.findViewById(R.id.noteDate);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }
}
