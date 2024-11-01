package com.example.tundra_snow_app;

import java.util.List;
import java.util.Set;

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
