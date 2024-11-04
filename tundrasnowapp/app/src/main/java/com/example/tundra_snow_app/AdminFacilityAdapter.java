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

public class AdminFacilityAdapter extends RecyclerView.Adapter<AdminFacilityAdapter.FacilityViewHolder> {

    private final List<Facilities> facilityList;
    private final Context context;
    private final FirebaseFirestore db;

    public AdminFacilityAdapter(Context context, List<Facilities> facilityList) {
        this.context = context;
        this.facilityList = facilityList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_facility_item, parent, false);
        return new FacilityViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

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

