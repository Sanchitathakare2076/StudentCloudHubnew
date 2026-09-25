package com.studentcloudhub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studentcloudhub.R;
import com.studentcloudhub.model.Question;

import java.util.List;

public class TestReviewAdapter extends RecyclerView.Adapter<TestReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<Question> questionList;

    public TestReviewAdapter(Context context, List<Question> questionList) {
        this.context = context;
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_test_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question q = questionList.get(position);

        holder.tvQuestionNum.setText("Question " + (position + 1));
        holder.tvQuestionText.setText(q.getQuestionText());

        boolean isCorrect = q.isCorrect();
        if (isCorrect) {
            holder.tvResultBadge.setText("✓ CORRECT");
            holder.tvResultBadge.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvYourAnswer.setText(q.getSelectedAnswerText());
            holder.tvYourAnswer.setTextColor(Color.parseColor("#2E7D32"));
            holder.layoutCorrectAnswer.setVisibility(View.GONE);
        } else {
            holder.tvResultBadge.setText("✕ INCORRECT");
            holder.tvResultBadge.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvYourAnswer.setText(q.getSelectedAnswerText());
            holder.tvYourAnswer.setTextColor(Color.parseColor("#D32F2F"));

            holder.layoutCorrectAnswer.setVisibility(View.VISIBLE);
            holder.tvCorrectAnswer.setText(q.getCorrectAnswerText());
            holder.tvCorrectAnswer.setTextColor(Color.parseColor("#2E7D32"));
        }
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNum;
        TextView tvResultBadge;
        TextView tvQuestionText;
        TextView tvYourAnswer;
        TextView tvCorrectAnswer;
        LinearLayout layoutCorrectAnswer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNum = itemView.findViewById(R.id.tvQuestionNum);
            tvResultBadge = itemView.findViewById(R.id.tvResultBadge);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvYourAnswer = itemView.findViewById(R.id.tvYourAnswer);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            layoutCorrectAnswer = itemView.findViewById(R.id.layoutCorrectAnswer);
        }
    }
}
