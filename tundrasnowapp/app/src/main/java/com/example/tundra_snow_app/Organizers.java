package com.example.tundra_snow_app;

public class Organizers extends Users {
    public Organizers(
            int userID,
            String firstName,
            String lastName,
            String email,
            String password,
            String profilePic,
            String dateOfBirth,

            int phoneNumber,
            boolean notificationsEnabled,

            int deviceID,
            boolean geolocationEnabled,
            String location
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
                geolocationEnabled,
                location
        );
    }

}
