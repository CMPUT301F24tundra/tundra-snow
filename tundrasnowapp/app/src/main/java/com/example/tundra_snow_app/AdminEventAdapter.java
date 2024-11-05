package com.example.tundra_snow_app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for the RecyclerView in the AdminEventsActivity. This class
 * is responsible for displaying the list of events in the RecyclerView.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private final List<Events> eventList; // List of events
    private final Context context; // Context of the activity
    private final FirebaseFirestore db; // Firestore database
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault()); // Date format
    private final OnDeleteClickListener onDeleteClickListener; // Listener for delete button
    
    /**
     * Constructor for the AdminEventAdapter class. Initializes the adapter with the
     * given context, list of events, and listener for the delete button.
     * @param context Context of the activity
     * @param eventList List of events
     * @param onDeleteClickListener Listener for delete button
     */
    public AdminEventAdapter(Context context, List<Events> eventList, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();
        this.onDeleteClickListener = onDeleteClickListener;
    }

    /**
     * Creates a new ViewHolder object for the RecyclerView.
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_active_events_item, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder.
     * @param holder The ViewHolder
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Events event = eventList.get(position);

        holder.eventName.setText(event.getTitle());
        holder.eventLocation.setText(event.getLocation());
        holder.eventDateTime.setText(event.getStartDate() != null ? dateFormat.format(event.getStartDate()) : "Date TBD");

        // Delete event when "Delete" button is clicked
        holder.removeEventButton.setOnClickListener(v -> {
            db.collection("events").document(event.getEventID())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                        if (onDeleteClickListener != null) {
                            onDeleteClickListener.onEventDeleted(position);  // Remove item from adapter
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting event", Toast.LENGTH_SHORT).show();
                        Log.e("AdminEventAdapter", "Error deleting event", e);
                    });
        });
    }

    /**
     * Returns the number of items in the list of events.
     * @return The number of items in the list of events
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Interface for the delete button click listener.
     */
    public interface OnDeleteClickListener {
        void onEventDeleted(int position);
    }

    /**
     * ViewHolder class for the RecyclerView.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventDateTime, eventName, eventLocation;
        Button removeEventButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventName = itemView.findViewById(R.id.eventName);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            removeEventButton = itemView.findViewById(R.id.removeEventButton);
        }
    }
}
