package com.example.tundra_snow_app;

import java.util.Date;

public class Events {
    // Variables
    private int eventID;
    private int ownerID;
    private String title;
    private String description;
    private String posterImageURL;
    private String location;
    private Date dateStart;
    private Date dateEnd;
    private Date registrationStart;
    private Date registrationEnd;
    private int capacity;
    private String qrHash;
    private String status;
    private int[] entrantList;
    private int[] confirmedList;
    private int[] declinedList;
    private int[] cancelledList;


    // Constructor

    public Events(
            int eventID,
            int ownerID,
            String title,
            String description,
            String posterImageURL,
            String location,
            Date dateStart,
            Date dateEnd,
            Date registrationEnd,
            Date registrationStart,
            int capacity,
            String qrHash,
            String status,
            int[] confirmedList,
            int[] declinedList,
            int[] entrantList,
            int[] cancelledList
    ) {
        this.eventID = eventID;
        this.ownerID = ownerID;
        this.title = title;
        this.description = description;
        this.posterImageURL = posterImageURL;
        this.location = location;
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

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
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

    public int[] getEntrantList() {
        return entrantList;
    }

    public void setEntrantList(int[] entrantList) {
        this.entrantList = entrantList;
    }

    public int[] getConfirmedList() {
        return confirmedList;
    }

    public void setConfirmedList(int[] confirmedList) {
        this.confirmedList = confirmedList;
    }

    public int[] getDeclinedList() {
        return declinedList;
    }

    public void setDeclinedList(int[] declinedList) {
        this.declinedList = declinedList;
    }

    public int[] getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(int[] cancelledList) {
        this.cancelledList = cancelledList;
    }
}
