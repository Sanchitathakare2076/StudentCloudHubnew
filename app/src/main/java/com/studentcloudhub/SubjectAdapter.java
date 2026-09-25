package com.studentcloudhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    public interface OnSubjectClick {
        void onClick(Subject subject);
    }

    private final List<Subject> subjects;
    private final OnSubjectClick listener;

    public SubjectAdapter(
            List<Subject> subjects,
            OnSubjectClick listener) {

        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Subject subject = subjects.get(position);

        holder.subjectName.setText(subject.getName());

        holder.pdfCount.setText("Study PDFs");

        holder.progressBar.setProgress(0);

        holder.progressText.setText("0%");

        holder.itemView.setOnClickListener(v ->
                listener.onClick(subject));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView subjectName;
        TextView pdfCount;
        TextView progressText;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectName =
                    itemView.findViewById(R.id.subjectName);

            pdfCount =
                    itemView.findViewById(R.id.pdfCount);

            progressText =
                    itemView.findViewById(R.id.progressText);

            progressBar =
                    itemView.findViewById(R.id.progressBar);
        }
    }
}