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

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private final List<Events> eventList;
    private final Context context;
    private final FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.getDefault());
    private final OnDeleteClickListener onDeleteClickListener;

    public AdminEventAdapter(Context context, List<Events> eventList, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_active_events_item, parent, false);
        return new EventViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public interface OnDeleteClickListener {
        void onEventDeleted(int position);
    }

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