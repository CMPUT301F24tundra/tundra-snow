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
import java.util.List;

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.NotificationsViewHolder> {
    private final List<Notifications> notificationsList;
    private final Context context;

    /**
     * Constructor for the NotificationsListAdapter class.
     * @param context The context of the activity
     * @param notificationsList The list of notifications
     */
    public NotificationsListAdapter(Context context, List<Notifications> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    /**
     * Creates a new ViewHolder instance.
     * @param parent The parent view group
     * @param viewType The view type
     * @return A new NotificationsViewHolder instance
     */
    @NonNull
    @Override
    public NotificationsListAdapter.NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_notifications_item, parent, false);
        return new NotificationsListAdapter.NotificationsViewHolder(view);
    }

    /**
     * Binds the data to the views in the RecyclerView.
     * @param holder The NotificationsViewHolder instance
     * @param position The position of the item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationsListAdapter.NotificationsViewHolder holder, int position) {
        // Get the current event
        Notifications notification = notificationsList.get(position);

        // Bind data to my_notification_item.xml views
        holder.titleTextView.setText(notification.getEventName());
        holder.notificationTextView.setText(notification.getText());

        Log.d("NotificationsAdapter", "Binding notification: " + notification.getNotificationID());

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            intent = new Intent(context, MyEventDetailActivity.class);

            // Pass event ID to either activity
            intent.putExtra("eventID", notification.getEventID());
            context.startActivity(intent);
        });
    }

    /**
     * Returns the number of items in the RecyclerView.
     * @return The number of items in the notifications list
     */
    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    /**
     * ViewHolder class for the event list items.
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
