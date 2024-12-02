package com.example.tundra_snow_app.EventAdapters;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.tundra_snow_app.EventActivities.CreateEventActivity;
import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter class for the RecyclerView in the event list view.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Events> eventList;
    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault());
    private final Context context;
    private final FirebaseFirestore db;


    /**
     * Constructor for the EventAdapter class.
     * @param context The context of the activity
     * @param eventList The list of events
     */
    public EventAdapter(Context context, List<Events> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new ViewHolder instance.
     * @param parent The parent view group
     * @param viewType The view type
     * @return A new EventViewHolder instance
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ongoing_item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds the data to the views in the RecyclerView.
     * @param holder The EventViewHolder instance
     * @param position The position of the item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Get the current event
        Events event = eventList.get(position);

        // Bind data to item_event.xml views
        holder.titleTextView.setText(event.getTitle());
        holder.locationTextView.setText(event.getLocation());

        // Format and set the event date if not null
        if (event.getStartDate() != null) {
            String formattedDate = event.getFormattedDate(event.getStartDate());
            holder.dateTextView.setText(formattedDate);
        } else {
            holder.dateTextView.setText("Date TBD");
        }

        loadImageIntoView(event.getEventID(), holder.eventIcon);

        holder.itemView.setOnClickListener(v -> {

            Intent intent;

            // If the event is a draft, go the Create Event page
            if(Objects.equals(event.getPublished(), "no")){
                intent = new Intent(context, CreateEventActivity.class);
            } else {
                intent = new Intent(context, EventDetailActivity.class);
            }

            // Pass event ID to either activity
            intent.putExtra("eventID", event.getEventID());
            intent.putExtra("fromEventView", true);
            context.startActivity(intent);
        });
    }

    /**
     * Loads the event image from Firestore and binds it to the specified ImageView.
     * @param eventID The ID of the event whose image is to be loaded
     * @param imageView The ImageView to bind the image to
     */
    private void loadImageIntoView(String eventID, ImageView imageView) {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.event_placeholder) // Optional placeholder
                                    .error(R.drawable.event_error) // Optional error image
                                    .into(imageView);
                        } else {
                            Log.e("EventAdapter", "Image URL is null or empty for eventID: " + eventID);
                            imageView.setImageResource(R.drawable.event_placeholder); // Fallback
                        }
                    } else {
                        Log.e("EventAdapter", "Document does not exist for eventID: " + eventID);
                        imageView.setImageResource(R.drawable.event_placeholder); // Fallback
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventAdapter", "Failed to fetch image URL for eventID: " + eventID, e);
                    imageView.setImageResource(R.drawable.event_error); // Fallback on failure
                });
    }

    /**
     * Returns the number of items in the RecyclerView.
     * @return The number of items in the event list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for the event list items.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, titleTextView, locationTextView;
        ImageView eventIcon;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.eventDateTime);
            titleTextView = itemView.findViewById(R.id.eventName);
            locationTextView = itemView.findViewById(R.id.eventLocation);
            eventIcon = itemView.findViewById(R.id.eventIcon);
        }
    }
}
