package com.example.tundra_snow_app.Models;

import java.util.List;

/**
 * Represents an organizer of events. It encapsulates details such as the organizer's
 * unique ID, first name, last name, email, password, profile picture, date of birth,
 * phone number, notification settings, device ID, location, roles, and list of facilities.
 */
public class Organizers extends Users {

    /**
     * Constructor for the Organizers class. Initializes the organizer with the given
     * details.
     * @param userID Unique ID of the organizer
     * @param firstName First name of the organizer
     * @param lastName Last name of the organizer
     * @param email Email of the organizer
     * @param password Password of the organizer
     * @param profilePic Profile picture of the organizer
     * @param dateOfBirth Date of birth of the organizer
     * @param phoneNumber Phone number of the organizer
     * @param notificationsEnabled Notification settings of the organizer
     * @param deviceID Device ID of the organizer
     * @param location Location of the organizer
     * @param roles Roles of the organizer
     * @param facilityList List of facilities the organizer is associated with
     */
    public Organizers(
            String userID,
            String firstName,
            String lastName,
            String email,
            String password,
            String profilePic,
            String dateOfBirth,
            String phoneNumber,
            boolean notificationsEnabled,
            boolean geolocationEnabled,
            String deviceID,
            //boolean geolocationEnabled,
            String location,
            List<String> roles,
            List<String> facilityList
    ) {
        super(
                userID,
                firstName,
                lastName,
                email,
                password,
                profilePic,
                dateOfBirth,
                phoneNumber,
                notificationsEnabled,
                geolocationEnabled,
                deviceID,
                //geolocationEnabled,
                location,
                roles,
                facilityList
        );
    }

}
