package com.example.tundra_snow_app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* To Add;


 */
public class Users {
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String profilePicUrl;
    private String dateOfBirth; // maybe change field value


    // maybe put phone number when user is applying to events? so they change phone num
    // for different events? US 01.02.01
    private String phoneNumber; // int right now, can maybe turn into a string
    private boolean notificationsEnabled; // US 01.04.03

    // organizers CAN add geolocation requirement for their event, so user
    // can have a location attribute if they have geolocation enabled?

    private String deviceID; // US 01.07.01
    private String location;

    // Roles and permissions
    private List<String> roles;
    private List<String> permissions;
    private List<String> userEventList;

    // Organizer-specific attributes
    private List<String> facilityList;
    private List<String> organizerEventList;


    public Users (
            String userID,
            String firstName,
            String lastName,
            String email,
            String password,
            String profilePicUrl,
            String dateOfBirth,
            String phoneNumber,
            boolean notificationsEnabled,
            String deviceID,
            String location,
            List<String> roles,
            List<String> facilityList
    ) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.notificationsEnabled = notificationsEnabled;
        this.deviceID = deviceID;
        this.location = location;
        this.roles = new ArrayList<>(roles != null ? roles : Set.of("user"));

        // Assigning permissions based on roles
        this.permissions = assignPermissions(this.roles);

        // Universal user-specific attribute
        this.userEventList = new ArrayList<>();

        // Initializing role-specific attributes
        assert roles != null;
        if (roles.contains("organizer")) {
            this.facilityList = new ArrayList<>(facilityList);
            this.organizerEventList = new ArrayList<>();
        }
    }

    private List<String> assignPermissions(List<String> roles) {
        Set<String> permissionsSet = new HashSet<>();

        for (String role : roles) {
            switch (role.toLowerCase()) {
                case "organizer":
                    permissionsSet.add("CREATE_EVENT");
                    permissionsSet.add("VIEW_ENTRANT_LIST");
                    permissionsSet.add("MANAGE_ENTRANTS");
                    permissionsSet.add("NOTIFY_ENTRANTS");
                    break;
                case "user":
                    permissionsSet.add("JOIN_WAITLIST");
                    permissionsSet.add("VIEW_EVENT_DETAILS");
                    permissionsSet.add("UPDATE_PROFILE");
                    break;
                case "admin":
                    permissionsSet.add("REMOVE_EVENT");
                    permissionsSet.add("REMOVE_PROFILE");
                    permissionsSet.add("BROWSE_EVENTS");
                    permissionsSet.add("BROWSE_PROFILES");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid role: " + role);
            }
        }

        return new ArrayList<>(permissionsSet);
    }

    // add other constructors for if users want to skip certain fields?
    public String getUserID() {
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getFacilityList() {
        if (roles.contains("organizer")) {
            return facilityList;
        }

        throw new UnsupportedOperationException("This user does not have the role of 'organizer'");
    }
    public List<String> getOrganizerEventList() {
        if (roles.contains("organizer")) {
            return organizerEventList;
        }

        throw new UnsupportedOperationException("This user does not have the role of 'organizer'");
    }

    public List<String> getUserEventList() {
        return userEventList;
    }

    // Setters
    public void setUserID(String userID) {
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

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRole(List<String> roles) {
        this.roles = roles;
        this.permissions = assignPermissions(roles);
    }


    // Helper methods for adding or removing roles
    public void addRole(String role) {
        roles.add(role);
        this.permissions = assignPermissions(roles);
    }

    public void removeRole(String role) {
        roles.remove(role);
        this.permissions = assignPermissions(roles);
    }

    public void addUserEvent(String eventID) {
        userEventList.add(eventID);
    }

    public void addFacility(String facility) {
        if (roles.contains("organizer")) {
            facilityList.add(facility);
        } else {
            throw new UnsupportedOperationException("This user does not have the role 'organizer'");
        }
    }

    public void addOrganizerEvent(String eventID) {
        if (roles.contains("organizer")) {
            organizerEventList.add(eventID);
        } else {
            throw new UnsupportedOperationException("This user does not have the role 'organizer'");
        }
    }
}
