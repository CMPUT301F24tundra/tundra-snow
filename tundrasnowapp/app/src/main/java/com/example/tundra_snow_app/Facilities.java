package com.example.tundra_snow_app;

public class Facilities {
    public String facilityID;
    public String facilityName;
    public String facilityLocation;

    public Facilities() {}

    public Facilities (String facilityName, String facilityLocation, String facilityID) {
        this.facilityLocation = facilityLocation;
        this.facilityName = facilityName;
        this.facilityID = facilityID;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityLocation() {
        return facilityLocation;
    }

    public void setFacilityLocation(String facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    public String getFacilityID() {
        return facilityID;
    }

    public void setFacilityID(String facilityID) {
        this.facilityID = facilityID;
    }
}
