package com.example.tundra_snow_app.ListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.tundra_snow_app.R;

import java.util.List;

/**
 * Adapter class for the ListView in the FacilityListActivity. This class
 * handles the list of facilities.
 */
public class FacilityListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> facilities;
    private final OnFacilityActionListener actionListener;

    /**
     * Constructor for the FacilityListAdapter class. Initializes the adapter with the given context,
     * @param context Context of the activity
     * @param facilities List of facilities
     * @param actionListener Listener for facility actions
     */
    public FacilityListAdapter(Context context, List<String> facilities, OnFacilityActionListener actionListener) {
        super(context, 0, facilities);
        this.context = context;
        this.facilities = facilities;
        this.actionListener = actionListener;
    }

    /**
     * Creates a new View for the ListView.
     * @param position The position of the item in the list
     * @param convertView The old view to reuse, if possible
     * @param parent The parent that this view will eventually be attached to
     * @return A new View for the ListView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.facility_item, parent, false);
        }

        String facility = facilities.get(position);
        TextView facilityTextView = convertView.findViewById(R.id.facilityName);
        facilityTextView.setText(facility);

        // Handle clicks for edit and delete
        convertView.setOnClickListener(v -> actionListener.onFacilityClicked(position, facility));

        return convertView;
    }

    /**
     * Interface for handling facility actions.
     */
    public interface OnFacilityActionListener {
        void onFacilityClicked(int position, String facility);
    }
}
