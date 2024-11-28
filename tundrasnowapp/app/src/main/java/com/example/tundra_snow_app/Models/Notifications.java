package com.example.tundra_snow_app.Models;

import java.util.ArrayList;
import java.util.List;

public class Notifications {
    // Notification properties
    private String notificationID;
    private List<String> userIDs;
    private String eventID;
    private String eventName;
    private String text;
    private String notificationType;

    /**
     * No-argument constructor (required by Firestore).
     */
    public Notifications() {}

    /**
     * Constructor for the Users class. Initializes the user's properties.
     * @param notificationID ID of notification
     * @param userIDs List of IDs of users notifications are being pushed to
     * @param eventID ID of event notification is from
     * @param eventName Name of event notification is from
     * @param text Notification text
     * @param notificationType Type of notification
     */
    public Notifications(
            String notificationID,
            List<String> userIDs,
            String eventID,
            String eventName,
            String text,
            String notificationType
    ) {
        this.notificationID = notificationID;
        this.userIDs = userIDs;
        this.eventID = eventID;
        this.eventName = eventName;
        this.text = text;
        this.notificationType = notificationType;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public List<String> getUserIDs() {
        return userIDs;
    }

    public void setUserIDs(List<String> userIDs) {
        this.userIDs = userIDs;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
