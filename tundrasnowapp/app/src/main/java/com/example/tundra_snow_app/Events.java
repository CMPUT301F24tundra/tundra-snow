package com.example.tundra_snow_app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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

    private Date startDate;
    private Date endDate;
    private Date registrationStartDate;
    private Date registrationEndDate;

    private List<String> entrantList;
    private List<String> confirmedList;
    private List<String> declinedList;
    private List<String> cancelledList;

    // Constructors

    // No-argument constructor (required by Firestore)
    public Events() {
        this.entrantList = new ArrayList<>();
        this.confirmedList = new ArrayList<>();
        this.declinedList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
    }

    // Constructor with all fields
    public Events(
            String eventID,
            String ownerID,
            String title,
            String description,
            String posterImageURL,
            String location,
            String published,
            Date startDate,
            Date endDate,
            Date registrationEndDate,
            Date registrationStartDate,
            int capacity,
            String qrHash,
            String status,
            List<String> confirmedList,
            List<String> declinedList,
            List<String> entrantList,
            List<String> cancelledList
    ) {
        this.eventID = eventID;
        this.organizer = ownerID;
        this.title = title;
        this.description = description;
        this.posterImageURL = posterImageURL;
        this.location = location;
        this.published = published;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationEndDate = registrationEndDate;
        this.registrationStartDate = registrationStartDate;
        this.capacity = capacity;
        this.qrHash = qrHash;
        this.status = status;
        this.entrantList = entrantList;
        this.confirmedList = confirmedList;
        this.declinedList = declinedList;
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

    public Date getEndDate() {
        return endDate;
    }

    public String getFormattedDateEnd() {
        Date date = endDate;
        return getFormattedDate(date);
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRegistrationStartDate() {
        return registrationStartDate;
    }

    public String getFormattedRegStart() {
        Date date = registrationStartDate;
        return getFormattedDate(date);
    }

    public void setRegistrationStartDate(Date registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getFormattedDateStart() {
        Date date = startDate;
        return getFormattedDate(date);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPublished() { return published; }

    public void setPublished(String published) { this.published = published; }

    public Date getRegistrationEndDate() {
        return registrationEndDate;
    }

    public String getFormattedRegDateEnd() {
        Date date = registrationEndDate;
        return getFormattedDate(date);
    }

    public void setRegistrationEndDate(Date registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
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

    public List<String> getEntrantList() {
        return entrantList;
    }

    public void setEntrantList(List<String> entrantList) {
        this.entrantList = entrantList;
    }

    public void addEntrant(String userID) {
        entrantList.add(userID);
    }

    public void removeEntrant(String userID) {
        entrantList.remove(userID);
    }


    public List<String> getConfirmedList() {
        return confirmedList;
    }

    public void setConfirmedList(List<String> confirmedList) {
        this.confirmedList = confirmedList;
    }

    public void addConfirmed(String userID) {
        confirmedList.add(userID);
    }

    public void removeConfirmed(String userID) {
        confirmedList.remove(userID);
    }

    public List<String> getDeclinedList() {
        return declinedList;
    }

    public void setDeclinedList(List<String> declinedList) {
        this.declinedList = declinedList;
    }

    public void addDeclined(String userID) {
        declinedList.add(userID);
    }

    public void removeDeclined(String userID) {
        declinedList.remove(userID);
    }

    public List<String> getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(List<String> cancelledList) {
        this.cancelledList = cancelledList;
    }

    public void addCancelled(String userID) {
        cancelledList.add(userID);
    }

    public void removeCancelled(String userID) {
        cancelledList.remove(userID);
    }

    public String getFormattedDate(Date date) {
        if (date == null) {
            return "Date TBD";  // or any default message
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        return dateFormat.format(date);
    }
}
