package com.example.tundra_snow_app.Models;

import java.util.List;

/**
 * Represents an organizer of events. It encapsulates details such as the organizer's
 * unique ID, first name, last name, email, password, profile picture, date of birth,
 * phone number, notification settings, device ID, location, roles, and list of facilities.
 */
public class Organizers extends Users {
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
                deviceID,
                //geolocationEnabled,
                location,
                roles,
                facilityList
        );
    }

}
