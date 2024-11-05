package com.example.tundra_snow_app;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a user of the application. It encapsulates details such as the user's
 * unique ID, first name, last name, email, password, profile picture, date of birth,
 * phone number, notification settings, device ID, location, roles, and permissions.
 */
public class Users {

    // User properties
    private String userID; // Unique user ID
    private String firstName; // First name of the user
    private String lastName; // Last name of the user
    private String email; // Email of the user
    private String password; // Password of the user
    private String profilePicUrl; // URL of the user's profile picture
    private Date dateOfBirth; // Date of birth of the user


    // ! maybe put phone number when user is applying to events? so they change 
    // ! phone num for different events? US 01.02.01
    
    private String phoneNumber; // Phone number of the user
    private boolean notificationsEnabled; // Flag to indicate if notifications are enabled

    // ! organizers CAN add geolocation requirement for their event, so user can have a 
    // ! location attribute if they have geolocation enabled?

    private String deviceID; // Device ID of the user
    private String location; // Geolocation of the user

    // Roles and permissions
    private List<String> roles; // List of roles assigned to the user
    private List<String> permissions; // List of permissions based on the user's roles
    private List<String> userEventList; // List of events the user is attending

    // Organizer-specific attributes
    private List<String> facilityList; // List of facilities the organizer manages
    private List<String> organizerEventList; // List of events the organizer is hosting

    /**
     * No-argument constructor (required by Firestore).
     */
    public Users() {}

    /**
     * Constructor for the Users class. Initializes the user's properties.
     * @param userID Unique user ID
     * @param firstName First name of the user
     * @param lastName Last name of the user
     * @param email Email of the user
     * @param password Password of the user
     * @param profilePicUrl URL of the user's profile picture
     * @param dateOfBirth Date of birth of the user
     * @param phoneNumber Phone number of the user
     * @param notificationsEnabled Flag to indicate if notifications are enabled
     * @param deviceID Device ID of the user
     * @param location Geolocation of the user
     * @param roles List of roles assigned to the user
     * @param facilityList List of facilities the organizer manages
     */
    public Users (
            String userID,
            String firstName,
            String lastName,
            String email,
            String password,
            String profilePicUrl,
            Date dateOfBirth,
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

    /**
     * Assigns permissions based on the roles assigned to the user.
     * @param roles The roles assigned to the user
     * @return List of permissions granted based on the roles
     */
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

    /**
     * Returns the first name of the user.
     * @return First name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name of the user.
     * @return Last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the email of the user.
     * @return Email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password of the user.
     * @return Password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the date of birth of the user.
     * @return Date of birth of the user
     */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Returns the URL of the user's profile picture.
     * @return URL of the user's profile picture
     */
    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    /**
     * Returns the phone number of the user.
     * @return Phone number of the user
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns whether notifications are enabled for the user.
     * @return True if notifications are enabled, false otherwise
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Returns the device ID of the user.
     * @return Device ID of the user
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Returns the location of the user.
     * @return Location of the user
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the roles assigned to the user.
     * @return List of roles assigned to the user
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Returns the permissions granted to the user based on their roles.
     * @return List of permissions granted to the user
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Returns the list of facilities the organizer manages.
     * @return List of facilities the organizer manages
     */
    public List<String> getFacilityList() {
        if (roles.contains("organizer")) {
            return facilityList;
        }

        throw new UnsupportedOperationException("This user does not have the role of 'organizer'");
    }

    /**
     * Returns the list of events the organizer is hosting.
     * @return List of events the organizer is hosting
     */
    public List<String> getOrganizerEventList() {
        if (roles.contains("organizer")) {
            return organizerEventList;
        }

        throw new UnsupportedOperationException("This user does not have the role of 'organizer'");
    }

    /**
     * Returns the list of events the user is attending.
     * @return List of events the user is attending
     */
    public List<String> getUserEventList() {
        return userEventList;
    }

    /**
     * Sets the unique ID of the user.
     * @param userID The unique ID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Sets the first name of the user.
     * @param firstName The first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the last name of the user.
     * @param lastName The last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the email of the user.
     * @param email The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the date of birth of the user.
     * @param dateOfBirth The date of birth to set
     */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Sets the password of the user.
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the URL of the user's profile picture.
     * @param profilePicUrl The URL of the profile picture to set
     */
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    /**
     * Sets the phone number of the user.
     * @param phoneNumber The phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets whether notifications are enabled for the user.
     * @param notificationsEnabled True if notifications are enabled, false otherwise
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Sets the device ID of the user.
     * @param deviceID The device ID to set
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * Sets the location of the user.
     * @param location The location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the roles of the user and assigns permissions based on the roles.
     * @param roles The roles to assign
     */
    public void setRole(List<String> roles) {
        this.roles = roles;
        this.permissions = assignPermissions(roles);
    }

    /**
     * Adds a role to the user's list of roles.
     * @param role The role to add
     */
    public void addRole(String role) {
        roles.add(role);
        this.permissions = assignPermissions(roles);
    }

    /**
     * Removes a role from the user's list of roles.
     * @param role The role to remove
     */
    public void removeRole(String role) {
        roles.remove(role);
        this.permissions = assignPermissions(roles);
    }

    /**
     * Adds an event to the user's list of events.
     * @param eventID The ID of the event to add
     */
    public void addUserEvent(String eventID) {
        userEventList.add(eventID);
    }

    /**
     * Adds a facility to the organizer's list of facilities.
     * @param facility The facility to add
     */
    public void addFacility(String facility) {
        if (roles.contains("organizer")) {
            facilityList.add(facility);
        } else {
            throw new UnsupportedOperationException("This user does not have the role 'organizer'");
        }
    }

    /**
     * Adds an event to the organizer's list of events.
     * @param eventID The ID of the event to add
     */
    public void addOrganizerEvent(String eventID) {
        if (roles.contains("organizer")) {
            organizerEventList.add(eventID);
        } else {
            throw new UnsupportedOperationException("This user does not have the role 'organizer'");
        }
    }
}
