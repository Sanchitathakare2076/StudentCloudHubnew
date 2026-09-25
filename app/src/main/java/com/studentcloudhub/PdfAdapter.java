package com.studentcloudhub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PdfAdapter
        extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    public interface OnPdfClick {
        void onClick(PdfModel pdf);
    }

    private final List<PdfModel> pdfs;
    private final OnPdfClick listener;

    public PdfAdapter(
            List<PdfModel> pdfs,
            OnPdfClick listener) {

        this.pdfs = pdfs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_pdf,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        PdfModel pdf =
                pdfs.get(position);

        holder.pdfName.setText(
                pdf.getName()
        );

        holder.pdfDate.setText(
                "PDF Study Material"
        );

        holder.itemView.setOnClickListener(
                v -> listener.onClick(pdf)
        );
    }

    @Override
    public int getItemCount() {
        return pdfs.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView pdfName;
        TextView pdfDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            pdfName =
                    itemView.findViewById(
                            R.id.pdfName
                    );

            pdfDate =
                    itemView.findViewById(
                            R.id.pdfDate
                    );
        }
    }
}