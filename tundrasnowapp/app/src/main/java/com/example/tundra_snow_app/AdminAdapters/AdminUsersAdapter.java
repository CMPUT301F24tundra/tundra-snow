package com.example.tundra_snow_app.AdminAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.Models.Users;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter class for the RecyclerView in the AdminUsersActivity. This class
 * is responsible for displaying the list of users in the RecyclerView.
 */
public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private final List<Users> userList; // List of users
    private final Context context; // Context of the activity
    private final FirebaseFirestore db; // Firestore database

    /**
     * Constructor for the AdminUsersAdapter class. Initializes the adapter with the
     * given context and list of users.
     * @param context Context of the activity
     * @param userList List of users
     */
    public AdminUsersAdapter(Context context, List<Users> userList) {
        this.context = context;
        this.userList = userList;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_users_item, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder.
     * @param holder The ViewHolder
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users user = userList.get(position);

        // Combine first and last name for display
        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                (user.getLastName() != null ? user.getLastName() : "");
        holder.userFullName.setText(fullName.trim().isEmpty() ? "Unknown Name" : fullName.trim());

        holder.userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email available");

        // Delete button functionality
        holder.removeUserButton.setOnClickListener(v -> {
            String userID = user.getUserID();
            db.collection("users").document(userID)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "User deleted successfully.", Toast.LENGTH_SHORT).show();
                        userList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, userList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting user.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Returns the number of items in the list.
     * @return The number of items in the list
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userFullName, userEmail;
        Button removeUserButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userFullName = itemView.findViewById(R.id.userFullName);
            userEmail = itemView.findViewById(R.id.userEmail);
            removeUserButton = itemView.findViewById(R.id.removeUserButton);
        }
    }
}
