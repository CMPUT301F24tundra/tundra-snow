package com.example.tundra_snow_app.EventAdapters;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.tundra_snow_app.EventActivities.CreateEventActivity;
import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.R;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for the RecyclerView in the event list view.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Events> eventList;
    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault());
    private final Context context;

    // mode
    private Boolean isDraft;

    /**
     * Constructor for the EventAdapter class.
     * @param context The context of the activity
     * @param eventList The list of events
     */
    public EventAdapter(Context context, List<Events> eventList, Boolean draft) {
        this.context = context;
        this.eventList = eventList;
        this.isDraft = draft;
    }

    /**
     * Constructor for the EventAdapter class with default isDraft value of false.
     * @param context The context of the activity
     * @param eventList The list of events
     */
    public EventAdapter(Context context, List<Events> eventList) {
        this(context, eventList, false);
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (isDraft) {
                intent = new Intent(context, CreateEventActivity.class);
            } else {
                intent = new Intent(context, EventDetailActivity.class);
            }

            // Pass event ID to either activity
            intent.putExtra("eventID", event.getEventID());

            // If it's a draft, add a flag to indicate editing mode
            if (isDraft) {
                intent.putExtra("isEditingDraft", true);
            }

            context.startActivity(intent);
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

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.eventDateTime);
            titleTextView = itemView.findViewById(R.id.eventName);
            locationTextView = itemView.findViewById(R.id.eventLocation);
        }
    }
}
