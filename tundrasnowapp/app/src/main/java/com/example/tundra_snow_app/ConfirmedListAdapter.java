package com.example.tundra_snow_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ConfirmedListAdapter extends RecyclerView.Adapter<ConfirmedListAdapter.UserViewHolder> {

    private final List<String> confirmedList;
    private final Context context;
    private final FirebaseFirestore db;
    private String eventID;

    public ConfirmedListAdapter(Context context, List<String> confirmedList, String eventID) {
        this.context = context;
        this.confirmedList = confirmedList;
        this.eventID = eventID;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.confirmed_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userId = confirmedList.get(position);

        // Fetch user data from Firestore
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                String email = documentSnapshot.getString("email");

                // Combine firstName and lastName
                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                holder.fullNameTextView.setText(fullName.trim().isEmpty() ? "Unknown" : fullName.trim());
                holder.emailTextView.setText(email != null ? email : "No email available");
            }
        });

        // Set actions for Cancel button
        holder.cancelUserButton.setOnClickListener(v -> {
            // Move user to cancelledList and remove from confirmedList
            db.collection("events").document(eventID)
                    .update(
                            "cancelledList", FieldValue.arrayUnion(userId),
                            "confirmedList", FieldValue.arrayRemove(userId)
                    )
                    .addOnSuccessListener(aVoid -> {
                        confirmedList.remove(userId);  // Update local list for immediate UI feedback
                        notifyDataSetChanged();
                        Toast.makeText(context, "User cancelled successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error selecting user.", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return confirmedList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, emailTextView;
        Button cancelUserButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullName);
            emailTextView = itemView.findViewById(R.id.userEmail);
            cancelUserButton = itemView.findViewById(R.id.cancelUserButton);
        }
    }
}

