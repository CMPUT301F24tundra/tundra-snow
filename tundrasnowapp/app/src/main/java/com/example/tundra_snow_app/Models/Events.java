package com.example.tundra_snow_app.Models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Represents an event within the application. It encapsulates details such as
 * the event's ID, organizer, title, description, dates, location, and participants.
 */
public class Events {

    // Event Properties
    private String eventID; // Unique identifier for the event
    private String organizer; // ID of the user who organized the event
    private String title; // Title of the event
    private String description; // Description of the event
    private String posterImageURL; // URL of the event's poster image
    private String location; // Physical or virtual location of the event
    private String published; // Publication status of the event
    private String qrHash; // Unique QR code hash for event validation
    private String status; // Current status of the event
    private int capacity; // Maximum number of participants allowed

    // Event Timing
    private Date startDate; // Start time of the event
    private Date endDate; // End time of the event
    private Date registrationStartDate; // Date registration opens for the event
    private Date registrationEndDate; // Date registration closes for the event

    // Event Lists
    private List<String> entrantList; // Users who signed up to attend the event
    private List<String> confirmedList; // Users who confirmed attendance
    private List<String> declinedList; // Users who declined attendance
    private List<String> cancelledList; // Users who cancelled their participation
    private List<String> chosenList; // Users chosen for special roles or tasks

    // Constructors

    /**
     * No-argument constructor (required by Firestore). Initializes empty lists for participants.
     */
    public Events() {
        this.entrantList = new ArrayList<>();
        this.confirmedList = new ArrayList<>();
        this.declinedList = new ArrayList<>();
        this.cancelledList = new ArrayList<>();
        this.chosenList = new ArrayList<>();
    }

    /**
     * Constructor for the Events class. Initializes the event's properties.
     * @param eventID Unique event identifier
     * @param ownerID Organizer's ID
     * @param title Event title
     * @param description Event description
     * @param posterImageURL URL of the poster image
     * @param location Event location
     * @param published Publication status
     * @param startDate Event start date
     * @param endDate Event end date
     * @param registrationEndDate Registration end date
     * @param registrationStartDate Registration start date
     * @param capacity Event capacity
     * @param qrHash QR code hash for validation
     * @param status Event status
     * @param confirmedList List of confirmed attendees
     * @param declinedList List of declined attendees
     * @param entrantList List of entrants
     * @param cancelledList List of cancelled participants
     * @param chosenList List of chosen participants
     */
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
            List<String> cancelledList,
            List<String> chosenList
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
        this.chosenList = chosenList;
    }

    // Getters/Setters

    /**
     * Returns the list of users chosen for special roles in the event.
     * @return List of chosen users
     */
    public List<String> getChosenList() {
        return chosenList;
    }

    /**
     * Sets the list of users chosen for special roles in the event.
     * @param chosenList List of chosen users
     */
    public void setChosenList(List<String> chosenList) {
        this.chosenList = chosenList;
    }

    /**
     * Adds a user to the list of chosen participants.
     * @param userID ID of the user to add
     */
    public void addChosen(String userID) {
        chosenList.add(userID);
    }

    /**
     * Removes a user from the list of chosen participants.
     * @param userID ID of the user to remove
     */
    public void removeChosen(String userID) {
        chosenList.remove(userID);
    }

    /**
     * Returns the unique identifier for the event.
     * @return Event ID
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the unique identifier for the event.
     * @param eventID Event ID
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Returns the ID of the user who organized the event.
     * @return Organizer's ID
     */
    public String getOrganizer() {
        return organizer;
    }

    /**
     * Sets the ID of the user who organized the event.
     * @param organizer Organizer's ID
     */
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    /**
     * Returns the title of the event.
     * @return Event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event.
     * @param title Event title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the event.
     * @return Event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     * @param description Event description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the URL of the event's poster image.
     * @return Poster image URL
     */
    public String getPosterImageURL() {
        return posterImageURL;
    }

    /**
     * Sets the URL of the event's poster image.
     * @param posterImageURL Poster image URL
     */
    public void setPosterImageURL(String posterImageURL) {
        this.posterImageURL = posterImageURL;
    }

    /**
     * Returns the start date of the event.
     * @return Start date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the event.
     * @param endDate End date
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the formatted end date of the event.
     * @return Formatted end date
     */
    public String getFormattedDateEnd() {
        Date date = endDate;
        return getFormattedDate(date);
    }

    /**
     * Returns the start date of the event.
     * @return Start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Returns the start date of the event.
     * @return Start date
     */
    public Date getRegistrationStartDate() {
        return registrationStartDate;
    }

    /**
     * Returns the formatted start date of the registration period.
     * @return Formatted start date
     */
    public String getFormattedRegStart() {
        Date date = registrationStartDate;
        return getFormattedDate(date);
    }

    /**
     * Sets the start date of the registration period.
     * @param registrationStartDate Registration start date
     */
    public void setRegistrationStartDate(Date registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    /**
     * Sets the start date of the event.
     * @param startDate Start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

        /**
     * Returns the formatted start date of the event.
     * @return Formatted start date
     */
    public String getFormattedDateStart() {
        Date date = startDate;
        return getFormattedDate(date);
    }

    /**
     * Returns the end date of the registration period.
     * @return Registration end date
     */
    public Date getRegistrationEndDate() {
        return registrationEndDate;
    }

    /**
     * Sets the end date of the registration period.
     * @param registrationEndDate Registration end date
     */
    public void setRegistrationEndDate(Date registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    /**
     * Returns the formatted end date of the registration period.
     * @return Formatted end date
     */
    public String getFormattedRegDateEnd() {
        Date date = registrationEndDate;
        return getFormattedDate(date);
    }

    /**
     * Returns the location of the event.
     * @return Event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     * @param location Event location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the publication status of the event.
     * @return Publication status
     */
    public String getPublished() { return published; }

    /**
     * Sets the publication status of the event.
     * @param published Publication status
     */
    public void setPublished(String published) { this.published = published; }
 
    /**
     * Returns the capacity of the event.
     * @return Event capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the capacity of the event.
     * @param capacity Event capacity
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns the QR code hash for event validation.
     * @return QR code hash
     */
    public String getQrHash() {
        return qrHash;
    }

    /**
     * Sets the QR code hash for event validation.
     * @param qrHash QR code hash
     */
    public void setQrHash(String qrHash) {
        this.qrHash = qrHash;
    }

    /**
     * Returns the current status of the event.
     * @return Event status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the event.
     * @param status Event status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the list of entrants (waiting list) for the event.
     * @return List of entrants
     */
    public List<String> getEntrantList() {
        return entrantList;
    }

    /**
     * Sets the list of entrants (waiting list) for the event.
     * @param entrantList List of entrants
     */
    public void setEntrantList(List<String> entrantList) {
        this.entrantList = entrantList;
    }

    /**
     * Adds a user to the list of entrants (waiting list) for the event.
     * @param userID ID of the user to add
     */
    public void addEntrant(String userID) {
        entrantList.add(userID);
    }

    /**
     * Removes a user from the list of entrants (waiting list) for the event.
     * @param userID ID of the user to remove
     */
    public void removeEntrant(String userID) {
        entrantList.remove(userID);
    }

    /**
     * Returns the list of confirmed attendees for the event.
     * @return List of confirmed attendees
     */
    public List<String> getConfirmedList() {
        return confirmedList;
    }

    /**
     * Sets the list of confirmed attendees for the event.
     * @param confirmedList List of confirmed attendees
     */
    public void setConfirmedList(List<String> confirmedList) {
        this.confirmedList = confirmedList;
    }

    /**
     * Adds a user to the list of confirmed attendees for the event.
     * @param userID ID of the user to add
     */
    public void addConfirmed(String userID) {
        confirmedList.add(userID);
    }

    /**
     * Removes a user from the list of confirmed attendees for the event.
     * @param userID ID of the user to remove
     */
    public void removeConfirmed(String userID) {
        confirmedList.remove(userID);
    }

    /**
     * Returns the list of declined attendees for the event.
     * @return List of declined attendees
     */
    public List<String> getDeclinedList() {
        return declinedList;
    }

    /**
     * Sets the list of declined attendees for the event.
     * @param declinedList List of declined attendees
     */
    public void setDeclinedList(List<String> declinedList) {
        this.declinedList = declinedList;
    }

    /**
     * Adds a user to the list of declined attendees for the event.
     * @param userID ID of the user to add
     */
    public void addDeclined(String userID) {
        declinedList.add(userID);
    }

    /**
     * Removes a user from the list of declined attendees for the event.
     * @param userID ID of the user to remove
     */
    public void removeDeclined(String userID) {
        declinedList.remove(userID);
    }

    /**
     * Returns the list of cancelled participants for the event.
     * @return List of cancelled participants
     */
    public List<String> getCancelledList() {
        return cancelledList;
    }

    /**
     * Sets the list of cancelled participants for the event.
     * @param cancelledList List of cancelled participants
     */
    public void setCancelledList(List<String> cancelledList) {
        this.cancelledList = cancelledList;
    }

    /**
     * Adds a user to the list of cancelled participants for the event.
     * @param userID ID of the user to add
     */
    public void addCancelled(String userID) {
        cancelledList.add(userID);
    }

    /**
     * Removes a user from the list of cancelled participants for the event.
     * @param userID ID of the user to remove
     */
    public void removeCancelled(String userID) {
        cancelledList.remove(userID);
    }

    /**
     * Returns a formatted date string for the given date.
     * @param date Date to format
     * @return Formatted date string
     */
    public String getFormattedDate(Date date) {
        if (date == null) {
            return "Date and time TBD";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        return dateFormat.format(date);
    }
}
