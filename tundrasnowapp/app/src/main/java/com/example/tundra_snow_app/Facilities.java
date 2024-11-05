package com.example.tundra_snow_app;

/**
 * The Facilities class represents a facility where an event can occur.
 * Each facility includes details such as a unique facility ID, the name 
 * of the facility, and its location.
 */
public class Facilities {
    public String facilityID; // Unique facility ID
    public String facilityName; // Name of the facility
    public String facilityLocation; // Location of the facility

    /**
     * No-argument constructor (required by Firestore).
     */
    public Facilities() {}

    /**
     * Constructor for the Facilities class. Initializes the facility's properties.
     * @param facilityName Name of the facility
     * @param facilityLocation Location of the facility
     * @param facilityID Unique facility ID
     */
    public Facilities (String facilityName, String facilityLocation, String facilityID) {
        this.facilityLocation = facilityLocation;
        this.facilityName = facilityName;
        this.facilityID = facilityID;
    }

    /**
     * Returns the name of the facility.
     * @return Name of the facility
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * Sets the name of the facility.
     * @param facilityName Name of the facility
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * Returns the location of the facility.
     * @return Location of the facility
     */
    public String getFacilityLocation() {
        return facilityLocation;
    }

    /**
     * Sets the location of the facility.
     * @param facilityLocation Location of the facility
     */
    public void setFacilityLocation(String facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    /**
     * Returns the unique facility ID.
     * @return Unique facility ID
     */
    public String getFacilityID() {
        return facilityID;
    }

    /**
     * Sets the unique facility ID.
     * @param facilityID Unique facility ID
     */
    public void setFacilityID(String facilityID) {
        this.facilityID = facilityID;
    }
}
