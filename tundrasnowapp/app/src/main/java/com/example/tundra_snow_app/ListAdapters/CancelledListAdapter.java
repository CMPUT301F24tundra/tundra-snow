package com.example.tundra_snow_app.ListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter class for the RecyclerView in the ViewCancelledParticipantListActivity. This class
 * handles the list of cancelled participants for an event.
 */
public class CancelledListAdapter extends RecyclerView.Adapter<CancelledListAdapter.UserViewHolder> {

    private final List<String> cancelledList;
    private final Context context;
    private final FirebaseFirestore db;
    private String eventID;

    /**
     * Constructor for the CancelledListAdapter class. Initializes the adapter with the
     * given context, list of cancelled participants, and the event ID.
     * @param context Context of the activity
     * @param cancelledList List of cancelled participants
     * @param eventID ID of the event
     */
    public CancelledListAdapter(Context context, List<String> cancelledList, String eventID) {
        this.context = context;
        this.cancelledList = cancelledList;
        this.eventID = eventID;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new ViewHolder object for the RecyclerView.
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cancelled_list_item, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder.
     * @param holder The ViewHolder
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userId = cancelledList.get(position);

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
        holder.removeUserButton.setOnClickListener(v -> {
            // Move user to cancelledList and remove from confirmedList
            db.collection("events").document(eventID)
                    .update(
                            "cancelledList", FieldValue.arrayRemove(userId)
                    )
                    .addOnSuccessListener(aVoid -> {
                        cancelledList.remove(userId);  // Update local list for immediate UI feedback
                        notifyDataSetChanged();
                        Toast.makeText(context, "User removed from cancelled list.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error selecting user.", Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Returns the number of items in the list of cancelled participants.
     * @return The number of items in the list of cancelled participants
     */
    @Override
    public int getItemCount() {
        return cancelledList.size();
    }

    /**
     * ViewHolder class for the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, emailTextView;
        Button removeUserButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullName);
            emailTextView = itemView.findViewById(R.id.userEmail);
            removeUserButton = itemView.findViewById(R.id.removeUserButtonUserButton);
        }
    }
}

