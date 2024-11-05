package com.example.tundra_snow_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private final List<Users> userList;
    private final Context context;
    private final FirebaseFirestore db;

    public AdminUsersAdapter(Context context, List<Users> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_users_item, parent, false);
        return new UserViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return userList.size();
    }

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
