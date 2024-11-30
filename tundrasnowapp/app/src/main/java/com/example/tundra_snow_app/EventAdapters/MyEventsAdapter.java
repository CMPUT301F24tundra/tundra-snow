package com.example.tundra_snow_app.EventAdapters;

import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.EventActivities.MyEventDetailActivity;
import com.example.tundra_snow_app.EventActivities.OrganizerEventDetailActivity;

import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for the RecyclerView in the My Events view.
 */
public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.EventViewHolder> {

    private final List<Events> eventList;
    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault());
    private final Context context;
    private final String currentUserID;
    private final FirebaseFirestore db;
    private boolean isOrganizerMode;
    private int requestCode;

    /**
     * Constructor for the MyEventsAdapter class.
     * @param context The context of the activity
     * @param eventList The list of events
     * @param currentUserID The current user's ID
     * @param isOrganizerMode The mode of the adapter
     * @param requestCode The request code for the activity result
     */
    public MyEventsAdapter(Context context, List<Events> eventList, String currentUserID, boolean isOrganizerMode, int requestCode) {
        this.context = context;
        this.eventList = eventList;
        this.currentUserID = currentUserID;
        this.db = FirebaseFirestore.getInstance();
        this.isOrganizerMode = isOrganizerMode;
        this.requestCode = requestCode;
        Log.d("MyEventsAdapter", "Adapter initialized in mode: " + (isOrganizerMode ? "Organizer" : "User"));
        Log.d("MyEventsAdapter", "User ID: " + currentUserID);
    }

    /**
     * Sets the mode of the adapter and refreshes the list.
     * @param isOrganizerMode The mode of the adapter
     */
    public void setMode(boolean isOrganizerMode) {
        this.isOrganizerMode = isOrganizerMode;
        Log.d("MyEventsAdapter", "Mode switched to: " + (isOrganizerMode ? "Organizer" : "User"));
        notifyDataSetChanged();  // Refresh the list when mode changes
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_events_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Get the current event
        Events event = eventList.get(position);

        // Bind data to item_event.xml views
        holder.titleTextView.setText(event.getTitle());
        holder.locationTextView.setText(event.getLocation());

        // Format and set the event date if not null
        if (event.getStartDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(event.getStartDate());
            holder.dateTextView.setText(formattedDate);
        } else {
            holder.dateTextView.setText("Date TBD");
        }

        // Fetch and display the user’s status only if in "User" mode
        if (!isOrganizerMode) {
            fetchUserStatus(event.getEventID(), holder.statusTextView);
            holder.statusTextView.setTextSize(10);
        } else {
            holder.statusTextView.setText(">");
            holder.statusTextView.setTextSize(20);
        }

        // Set the onClickListener with a conditional to check the mode
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MyEventDetailActivity.class);
            if (isOrganizerMode) {
                // Organizer mode: Navigate to OrganizerEventActivity
                intent = new Intent(context, OrganizerEventDetailActivity.class);
            } else {
                // User mode: Navigate to EventDetailActivity
                intent = new Intent(context, MyEventDetailActivity.class);
            }
            intent.putExtra("eventID", event.getEventID());  // Pass event ID to the detail activity
            ((Activity) context).startActivityForResult(intent, requestCode);
        });
    }

    private void fetchUserStatus(String eventID, TextView statusTextView) {
        // Declare lists as final to avoid reassigning them
        final List<String> entrantList = new ArrayList<>();
        final List<String> confirmedList = new ArrayList<>();
        final List<String> cancelledList = new ArrayList<>();
        final List<String> declinedList = new ArrayList<>();
        final List<String> chosenList = new ArrayList<>();

        // Query the database to determine the user's status for this event
        db.collection("events").document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve lists, using temporary lists for assignment
                        List<String> tempEntrantList = (List<String>) documentSnapshot.get("entrantList");
                        List<String> tempConfirmedList = (List<String>) documentSnapshot.get("confirmedList");
                        List<String> tempCancelledList = (List<String>) documentSnapshot.get("cancelledList");
                        List<String> tempDeclinedList = (List<String>) documentSnapshot.get("declinedList");
                        List<String> tempChosenList = (List<String>) documentSnapshot.get("chosenList");

                        // Populate final lists without reassigning
                        if (tempEntrantList != null) entrantList.addAll(tempEntrantList);
                        if (tempConfirmedList != null) confirmedList.addAll(tempConfirmedList);
                        if (tempCancelledList != null) cancelledList.addAll(tempCancelledList);
                        if (tempDeclinedList != null) declinedList.addAll(tempDeclinedList);
                        if (tempChosenList != null) chosenList.addAll(tempChosenList);

                        // Log the retrieved lists for troubleshooting
                        Log.d("MyEventsView", "EntrantList: " + entrantList);
                        Log.d("MyEventsView", "ConfirmedList: " + confirmedList);
                        Log.d("MyEventsView", "CancelledList: " + cancelledList);
                        Log.d("MyEventsView", "DeclinedList: " + declinedList);
                        Log.d("MyEventsView", "ChosenList: " + chosenList);

                        // Determine and set status based on lists, ensuring null-safe checks
                        String status;
                        if (entrantList.contains(currentUserID) &&
                                (confirmedList.isEmpty() || !confirmedList.contains(currentUserID)) &&
                                (cancelledList.isEmpty() || !cancelledList.contains(currentUserID)) &&
                                (declinedList.isEmpty() || !declinedList.contains(currentUserID))) {
                            Log.d("MyEventsAdapter", "User in entrant list");
                            status = "Pending...";
                        } else if (confirmedList.contains(currentUserID)) {
                            Log.d("MyEventsAdapter", "User in confirmed list");
                            status = "Accepted!";
                        } else if (cancelledList.contains(currentUserID)) {
                            Log.d("MyEventsAdapter", "User in cancelled List");
                            status = "Cancelled.";
                        } else if (declinedList.contains(currentUserID)) {
                            Log.d("MyEventsAdapter", "User in declined list");
                            status = "Declined.";
                        } else if (chosenList.contains(currentUserID)) {
                            Log.d("MyEventsAdapter", "User in chosen list");
                            status = "Invited!";
                        } else {
                            status = "";  // No status if not found in any list
                        }
                        Log.d("MyEventsView", "Status for event ID " + eventID + ": " + status);

                        // Update the UI with the status
                        statusTextView.setText(status);
                    } else {
                        Log.d("MyEventsView", "Event document not found for ID: " + eventID);
                        statusTextView.setText("Unknown Status");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("MyEventsView", "Error fetching event status for ID: " + eventID, e);
                    statusTextView.setText("Error");
                });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, titleTextView, locationTextView, statusTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.eventDateTime);
            titleTextView = itemView.findViewById(R.id.eventName);
            locationTextView = itemView.findViewById(R.id.eventLocation);
            statusTextView = itemView.findViewById(R.id.eventStatus);
        }
    }
}
