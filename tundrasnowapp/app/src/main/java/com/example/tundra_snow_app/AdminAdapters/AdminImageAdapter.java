package com.example.tundra_snow_app.AdminAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ViewHolder> {
    private final Context context;
    private final List<String> imageUrls;
    private final Map<String, String> imageUrlToEventIdMap;

    public AdminImageAdapter(Context context, List<String> imageUrls, Map<String, String> imageUrlToEventIdMap) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.imageUrlToEventIdMap = imageUrlToEventIdMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context).load(imageUrl).into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> removeImage(position))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void removeImage(int position) {
        String imageUrl = imageUrls.get(position);
        String eventId = imageUrlToEventIdMap.get(imageUrl);

        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

        // Delete the image from Firebase Storage
        photoRef.delete().addOnSuccessListener(aVoid -> {
            // Remove image URL from the list and notify adapter
            imageUrls.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Image removed successfully.", Toast.LENGTH_SHORT).show();

            // Update Firestore to set imageUrl to null
            updateImageUrlInDatabase(eventId);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to remove image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateImageUrlInDatabase(String eventId) {
        if (eventId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events")
                    .document(eventId)
                    .update("imageUrl", null) // Set imageUrl to null
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Database updated successfully.", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to update database: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(context, "Event ID not found for the image.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
