package com.example.tundra_snow_app.ListAdapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    // Add this method to allow dynamic updates
    public void updateData(List<String> newUserList) {
        this.entrantList.clear();
        this.entrantList.addAll(newUserList);
        notifyDataSetChanged(); // Notify the adapter of changes
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

                loadImageIntoView(userId, holder.userIcon);
            }
        });
    }

    /**
     * Loads the user image from Firestore and binds it to the specified ImageView.
     * @param userID The ID of the event whose image is to be loaded
     * @param imageView The ImageView to bind the image to
     */
    private void loadImageIntoView(String userID, ImageView imageView) {
        db.collection("users").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profilePictureUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_profile_picture) // Optional placeholder
                                    .error(R.drawable.event_error) // Optional error image
                                    .into(imageView);
                        } else {
                            Log.e("EventAdapter", "Image URL is null or empty for userID: " + userID);
                            imageView.setImageResource(R.drawable.default_profile_picture); // Fallback
                        }
                    } else {
                        Log.e("EventAdapter", "Document does not exist for userID: " + userID);
                        imageView.setImageResource(R.drawable.default_profile_picture); // Fallback
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventAdapter", "Failed to fetch image URL for userID: " + userID, e);
                    imageView.setImageResource(R.drawable.event_error); // Fallback on failure
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
        ImageView userIcon;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullName);
            emailTextView = itemView.findViewById(R.id.userEmail);
            userIcon = itemView.findViewById(R.id.userIcon);
        }
    }
}

