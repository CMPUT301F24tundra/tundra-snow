package com.example.tundra_snow_app.ListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.tundra_snow_app.R;

import java.util.List;

public class FacilityListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> facilities;
    private final OnFacilityActionListener actionListener;

    public FacilityListAdapter(Context context, List<String> facilities, OnFacilityActionListener actionListener) {
        super(context, 0, facilities);
        this.context = context;
        this.facilities = facilities;
        this.actionListener = actionListener;
    }

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

    public interface OnFacilityActionListener {
        void onFacilityClicked(int position, String facility);
    }
}
