package com.example.tundra_snow_app;

/* To Add;


 */
public class Users {
    private int userID;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String profilePic;
    private String dateOfBirth; // maybe change field value

    // maybe put phone number when user is applying to events? so they change phone num
    // for different events? US 01.02.01
    private int phoneNumber; // int right now, can maybe turn into a string
    private boolean notificationsEnabled; // US 01.04.03

    // organizers CAN add geolocation requirement for their event, so user
    // can have a location attribute if they have geolocation enabled?

    private int deviceID; // US 01.07.01
    private boolean geolocationEnabled;
    private String location;


    public Users (
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
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profilePic = profilePic;
        this.dateOfBirth = dateOfBirth;

        this.phoneNumber = phoneNumber;
        this.notificationsEnabled = notificationsEnabled;


        this.deviceID = deviceID;
        this.geolocationEnabled = geolocationEnabled;
        this.location = location;
    }

    // add other constructors for if users want to skip certain fields?
    public int getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public String getLocation() {
        return location;
    }

    // Setters
    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
