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
 * Adapter class for the RecyclerView in the UserListActivity. This class
 * handles the list of entrants for an event.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final List<String> entrantList;
    private final Context context;
    private final FirebaseFirestore db;
    private String eventID;

    /**
     * Constructor for the UserListAdapter class. Initializes the adapter with the given context,
     * @param context Context of the activity
     * @param entrantList List of entrants
     * @param eventID ID of the event
     */
    public UserListAdapter(Context context, List<String> entrantList, String eventID) {
        this.context = context;
        this.entrantList = entrantList;
        this.eventID = eventID;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new ViewHolder for the RecyclerView.
     * @param parent The parent that this view will eventually be attached to
     * @param viewType The type of the new view
     * @return A new ViewHolder for the RecyclerView
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder.
     * @param holder The ViewHolder
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userId = entrantList.get(position);

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

        // Set actions for Select and Reject buttons
        holder.selectUserButton.setOnClickListener(v -> {
            // Move user to chosenList and remove from entrantList
            db.collection("events").document(eventID)
                    .update(
                            "chosenList", FieldValue.arrayUnion(userId),
                            "entrantList", FieldValue.arrayRemove(userId)
                    )
                    .addOnSuccessListener(aVoid -> {
                        entrantList.remove(userId);  // Update local list for immediate UI feedback
                        notifyDataSetChanged();
                        Toast.makeText(context, "User selected successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error selecting user.", Toast.LENGTH_SHORT).show());
        });
        holder.rejectUserButton.setOnClickListener(v -> {
            db.collection("events").document(eventID)
                    .update(
                            "declinedList", FieldValue.arrayUnion(userId),
                            "entrantList", FieldValue.arrayRemove(userId)
                    )
                    .addOnSuccessListener(aVoid -> {
                        entrantList.remove(userId);
                        notifyDataSetChanged();
                        Toast.makeText(context, "User rejected successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error rejecting user.", Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Returns the number of items in the list of entrants.
     * @return The number of items in the list of entrants
     */
    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    /**
     * ViewHolder class for the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, emailTextView;
        Button selectUserButton, rejectUserButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullName);
            emailTextView = itemView.findViewById(R.id.userEmail);
            selectUserButton = itemView.findViewById(R.id.selectUser);
            rejectUserButton = itemView.findViewById(R.id.rejectUser);
        }
    }
}

