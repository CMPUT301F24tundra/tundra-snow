package com.example.tundra_snow_app.AdminAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

public class AdminQRAdapter extends RecyclerView.Adapter<AdminQRAdapter.ViewHolder> {

    private final Context context;
    private final List<String> eventTitles;
    private final List<String> qrHashes;
    private final List<String> eventIds;
    private final OnDeleteClickListener deleteClickListener;

    public AdminQRAdapter(Context context, List<String> eventTitles, List<String> qrHashes, List<String> eventIds, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.eventTitles = eventTitles;
        this.qrHashes = qrHashes;
        this.eventIds = eventIds;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_qr_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = eventTitles.get(position);
        String qrHash = qrHashes.get(position);

        // Set the event title in the TextView
        holder.eventTitleText.setText(title);

        // Generate and display the QR code
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            Bitmap qrBitmap = barcodeEncoder.encodeBitmap(qrHash, BarcodeFormat.QR_CODE, 400, 400);
            holder.qrImageView.setImageBitmap(qrBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Handle the delete button
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete QR Hash")
                    .setMessage("Are you sure you want to delete this QR Hash?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteClickListener.onDelete(position))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return eventTitles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitleText;
        ImageView qrImageView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitleText = itemView.findViewById(R.id.qrHashText); // Use for event title
            qrImageView = itemView.findViewById(R.id.eventIcon); // QR code ImageView
            deleteButton = itemView.findViewById(R.id.removeQRButton); // Delete button
        }
    }

    // Interface for delete button click
    public interface OnDeleteClickListener {
        void onDelete(int position);
    }
}
