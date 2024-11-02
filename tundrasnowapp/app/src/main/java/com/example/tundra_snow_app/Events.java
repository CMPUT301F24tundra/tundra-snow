package com.example.tundra_snow_app;

import java.util.Date;

public class Events {
    // Variables
    private String eventID;
    private String organizer;
    private String title;
    private String description;
    private String posterImageURL;
    private String location;
    private String published;
    private String qrHash;
    private String status;
    private int capacity;

    private Date dateStart;
    private Date dateEnd;
    private Date registrationStart;
    private Date registrationEnd;

    private String[] entrantList;
    private String[] confirmedList;
    private String[] declinedList;
    private String[] cancelledList;

    // Constructors

    // No-argument constructor (required for Firebase)
    public Events() {}

    // Constructor with all fields
    public Events(
            String eventID,
            String ownerID,
            String title,
            String description,
            String posterImageURL,
            String location,
            String published,
            Date dateStart,
            Date dateEnd,
            Date registrationEnd,
            Date registrationStart,
            int capacity,
            String qrHash,
            String status,
            String[] confirmedList,
            String[] declinedList,
            String[] entrantList,
            String[] cancelledList
    ) {
        this.eventID = eventID;
        this.organizer = ownerID;
        this.title = title;
        this.description = description;
        this.posterImageURL = posterImageURL;
        this.location = location;
        this.published = published;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.registrationEnd = registrationEnd;
        this.registrationStart = registrationStart;
        this.capacity = capacity;
        this.qrHash = qrHash;
        this.status = status;
        this.confirmedList = confirmedList;
        this.declinedList = declinedList;
        this.entrantList = entrantList;
        this.cancelledList = cancelledList;
    }

    // Getters/Setters

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterImageURL() {
        return posterImageURL;
    }

    public void setPosterImageURL(String posterImageURL) {
        this.posterImageURL = posterImageURL;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Date getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(Date registrationStart) {
        this.registrationStart = registrationStart;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPublished() { return published; }

    public void setPublished(String published) { this.published = published; }

    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getQrHash() {
        return qrHash;
    }

    public void setQrHash(String qrHash) {
        this.qrHash = qrHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getEntrantList() {
        return entrantList;
    }

    public void setEntrantList(String[] entrantList) {
        this.entrantList = entrantList;
    }

    public String[] getConfirmedList() {
        return confirmedList;
    }

    public void setConfirmedList(String[] confirmedList) {
        this.confirmedList = confirmedList;
    }

    public String[] getDeclinedList() {
        return declinedList;
    }

    public void setDeclinedList(String[] declinedList) {
        this.declinedList = declinedList;
    }

    public String[] getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(String[] cancelledList) {
        this.cancelledList = cancelledList;
    }
}
