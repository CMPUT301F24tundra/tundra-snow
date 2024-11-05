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

/**
 * Adapter class for the RecyclerView in the AdminFacilityViewActivity. This class
 * is responsible for displaying the list of facilities in the RecyclerView.
 */
public class AdminFacilityAdapter extends RecyclerView.Adapter<AdminFacilityAdapter.FacilityViewHolder> {

    private final List<Facilities> facilityList;
    private final Context context;
    private final FirebaseFirestore db;

    /**
     * Constructor for the AdminFacilityAdapter class. Initializes the adapter with the
     * given context and list of facilities.
     * @param context Context of the activity
     * @param facilityList List of facilities
     */
    public AdminFacilityAdapter(Context context, List<Facilities> facilityList) {
        this.context = context;
        this.facilityList = facilityList;
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
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_facility_item, parent, false);
        return new FacilityViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder.
     * @param holder The ViewHolder
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        Facilities facility = facilityList.get(position);

        holder.facilityName.setText(facility.getFacilityName());
        holder.facilityLocation.setText(facility.getFacilityLocation());

        // Delete button functionality
        holder.removeFacilityButton.setOnClickListener(v -> {
            String facilityID = facility.getFacilityID();
            db.collection("facilities").document(facilityID)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Facility deleted successfully.", Toast.LENGTH_SHORT).show();
                        facilityList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, facilityList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting facility.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Returns the number of items in the list.
     * @return Number of items in the list
     */
    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    /**
     * ViewHolder class for the RecyclerView.
     */
    public static class FacilityViewHolder extends RecyclerView.ViewHolder {
        TextView facilityName, facilityLocation;
        Button removeFacilityButton;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityName = itemView.findViewById(R.id.facilityName);
            facilityLocation = itemView.findViewById(R.id.facilityLocation);
            removeFacilityButton = itemView.findViewById(R.id.removeEventButton);
        }
    }
}

