package com.studentcloudhub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.studentcloudhub.R;
import com.studentcloudhub.model.StudyPlannerModel;

import java.util.List;

public class StudyPlannerAdapter extends RecyclerView.Adapter<StudyPlannerAdapter.ViewHolder> {

    public interface OnTaskActionListener {
        void onToggleComplete(StudyPlannerModel model, boolean isCompleted);
        void onEditTask(StudyPlannerModel model);
        void onDeleteTask(StudyPlannerModel model);
    }

    private final Context context;
    private final List<StudyPlannerModel> list;
    private final OnTaskActionListener listener;

    public StudyPlannerAdapter(Context context, List<StudyPlannerModel> list, OnTaskActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_study_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyPlannerModel model = list.get(position);

        holder.tvSubject.setText(model.getSubject() != null ? model.getSubject() : "Subject");
        holder.tvTopic.setText(model.getTopic() != null ? model.getTopic() : "Topic");

        String priority = model.getPriority() != null ? model.getPriority().toUpperCase() : "MEDIUM";
        holder.tvPriority.setText(priority);

        if ("HIGH".equalsIgnoreCase(priority)) {
            holder.tvPriority.setTextColor(Color.parseColor("#D32F2F")); // Red
        } else if ("LOW".equalsIgnoreCase(priority)) {
            holder.tvPriority.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else {
            holder.tvPriority.setTextColor(Color.parseColor("#E65100")); // Orange
        }

        if (model.getDescription() != null && !model.getDescription().trim().isEmpty()) {
            holder.tvDescription.setText(model.getDescription().trim());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        String dateStr = model.getDate() != null ? model.getDate() : "";
        holder.tvDate.setText("📅 " + dateStr);

        String start = model.getStartTime() != null ? model.getStartTime() : "";
        String end = model.getEndTime() != null ? model.getEndTime() : "";
        if (!start.isEmpty() || !end.isEmpty()) {
            holder.tvTime.setText("⏰ " + start + (end.isEmpty() ? "" : " - " + end));
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        boolean isCompleted = model.isCompleted();

        // Prevent check change loop during view binding
        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(isCompleted);

        if (isCompleted) {
            holder.tvTopic.setPaintFlags(holder.tvTopic.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.cardStudyPlan.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
        } else {
            holder.tvTopic.setPaintFlags(holder.tvTopic.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
            holder.cardStudyPlan.setCardBackgroundColor(Color.WHITE);
        }

        holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleComplete(model, isChecked);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTask(model);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTask(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardStudyPlan;
        CheckBox cbComplete;
        TextView tvSubject;
        TextView tvPriority;
        TextView tvTopic;
        TextView tvDescription;
        TextView tvDate;
        TextView tvTime;
        TextView tvStatus;
        ImageView btnEdit;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStudyPlan = itemView.findViewById(R.id.cardStudyPlan);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
