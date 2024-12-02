package com.example.tundra_snow_app.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a notification in the Tundra Snow app. Notifications are associated with an event
 * and are sent to a list of users, containing details such as the event name, notification type,
 * and status for each user.
 */
public class Notifications {
    // Notification properties
    private String notificationID;
    private List<String> userIDs;
    private String eventID;
    private String eventName;
    private String text;
    private String notificationType;
    private Map<String, Boolean> userStatus;

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
     * @param userStatus Whether notification sent is new or not for all users in userIDs
     */
    public Notifications(
            String notificationID,
            List<String> userIDs,
            String eventID,
            String eventName,
            String text,
            String notificationType,
            Map<String, Boolean> userStatus
    ) {
        this.notificationID = notificationID;
        this.userIDs = userIDs;
        this.eventID = eventID;
        this.eventName = eventName;
        this.text = text;
        this.notificationType = notificationType;
        this.userStatus = userStatus;
    }

    /**
     * Retrieves the user status map, indicating whether the notification is new for each user.
     *
     * @return A map where keys are user IDs and values are booleans (true if new, false otherwise).
     */
    public Map<String, Boolean> getUserStatus() {
        return userStatus;
    }

    /**
     * Sets the user status map for the notification.
     *
     * @param userStatus A map where keys are user IDs and values are booleans (true if new, false otherwise).
     */
    public void setUserStatus(Map<String, Boolean> userStatus) {
        this.userStatus = userStatus;
    }

    /**
     * Retrieves the unique ID of the notification.
     *
     * @return The notification ID as a string.
     */
    public String getNotificationID() {
        return notificationID;
    }

    /**
     * Sets the unique ID for the notification.
     *
     * @param notificationID The notification ID as a string.
     */
    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    /**
     * Retrieves the list of user IDs to whom the notification is sent.
     *
     * @return A list of user IDs.
     */
    public List<String> getUserIDs() {
        return userIDs;
    }

    /**
     * Sets the list of user IDs to whom the notification is sent.
     *
     * @param userIDs A list of user IDs.
     */
    public void setUserIDs(List<String> userIDs) {
        this.userIDs = userIDs;
    }

    /**
     * Retrieves the type or category of the notification.
     *
     * @return The notification type as a string.
     */
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * Sets the type or category for the notification.
     *
     * @param notificationType The notification type as a string.
     */
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    /**
     * Retrieves the ID of the event associated with the notification.
     *
     * @return The event ID as a string.
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the ID of the event associated with the notification.
     *
     * @param eventID The event ID as a string.
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Retrieves the name of the event associated with the notification.
     *
     * @return The event name as a string.
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the name of the event associated with the notification.
     *
     * @param eventName The event name as a string.
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Retrieves the text content of the notification.
     *
     * @return The notification text as a string.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the notification.
     *
     * @param text The notification text as a string.
     */
    public void setText(String text) {
        this.text = text;
    }
}
