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


/**
 * Adapter class for the RecyclerView in the notifications list view.
 * This adapter displays a list of notifications for the current user and handles
 * interactions with notifications, such as marking them as viewed.
 */
public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.NotificationsViewHolder> {
    private final List<Notifications> notificationsList;
    private final Context context;
    private final String currentUserID;

    /**
     * Initializes the NotificationsListAdapter with the context, notifications list, and the current user ID.
     *
     * @param context The context of the activity.
     * @param notificationsList The list of notifications to display.
     * @param currentUserID The ID of the current user to check notification statuses.
     */
    public NotificationsListAdapter(Context context, List<Notifications> notificationsList, String currentUserID) {
        this.context = context;
        this.notificationsList = notificationsList;
        this.currentUserID = currentUserID;
    }

    /**
     * Creates a new ViewHolder for a notification item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new NotificationsViewHolder instance that holds a View for a notification.
     */
    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_notifications_item, parent, false);
        return new NotificationsViewHolder(view);
    }

    /**
     * Binds the notification data to the ViewHolder and sets up interaction logic.
     * Highlights the notification if it is new for the current user and adds a click listener
     * to navigate to the event details screen when clicked.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
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

    /**
     * Marks the specified notification as viewed for the current user in Firestore.
     *
     * @param notification The notification to mark as viewed.
     */
    private void markNotificationAsViewed(Notifications notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .document(notification.getNotificationID())
                .update("userStatus." + currentUserID, false)
                .addOnSuccessListener(aVoid -> Log.d("NotificationsAdapter", "Marked as viewed"))
                .addOnFailureListener(e -> Log.e("NotificationsAdapter", "Failed to mark as viewed", e));
    }

    /**
     * Returns the total number of notifications in the list.
     *
     * @return The size of the notifications list.
     */
    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    /**
     * ViewHolder class for the RecyclerView items.
     * Holds the views for displaying a notification's event name and text content.
     */
    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, notificationTextView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventName);
            notificationTextView = itemView.findViewById(R.id.notificationText);
        }
    }
}
