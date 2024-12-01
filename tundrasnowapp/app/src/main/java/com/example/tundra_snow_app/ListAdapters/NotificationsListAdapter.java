package com.example.tundra_snow_app.ListAdapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.EventActivities.MyEventDetailActivity;
import com.example.tundra_snow_app.Models.Notifications;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.NotificationsViewHolder> {
    private final List<Notifications> notificationsList;
    private final Context context;
    private final String currentUserID;

    public NotificationsListAdapter(Context context, List<Notifications> notificationsList, String currentUserID) {
        this.context = context;
        this.notificationsList = notificationsList;
        this.currentUserID = currentUserID;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_notifications_item, parent, false);
        return new NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {
        Notifications notification = notificationsList.get(position);

        holder.titleTextView.setText(notification.getEventName());
        holder.notificationTextView.setText(notification.getText());

        // Check if the notification is new for the current user
        Boolean isNewForUser = notification.getUserStatus().get(currentUserID);

        // Debug log to confirm the status
        Log.d("NotificationsAdapter", "Notification ID: " + notification.getNotificationID() +
                ", isNewForUser: " + isNewForUser);

        // Apply highlight if the notification is new for the user
        if (isNewForUser != null && isNewForUser) {
            Log.d("NotificationsAdapter", "Applying highlight for notification: " + notification.getNotificationID());
            holder.itemView.setBackgroundResource(R.drawable.notification_highlight);
        } else {
            Log.d("NotificationsAdapter", "Removing highlight for notification: " + notification.getNotificationID());
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        // Handle click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MyEventDetailActivity.class);
            intent.putExtra("eventID", notification.getEventID());
            context.startActivity(intent);

            // Mark the notification as viewed for the current user
            if (isNewForUser != null && isNewForUser) {
                markNotificationAsViewed(notification);
            }
        });
    }

    private void markNotificationAsViewed(Notifications notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .document(notification.getNotificationID())
                .update("userStatus." + currentUserID, false)
                .addOnSuccessListener(aVoid -> Log.d("NotificationsAdapter", "Marked as viewed"))
                .addOnFailureListener(e -> Log.e("NotificationsAdapter", "Failed to mark as viewed", e));
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, notificationTextView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventName);
            notificationTextView = itemView.findViewById(R.id.notificationText);
        }
    }
}
