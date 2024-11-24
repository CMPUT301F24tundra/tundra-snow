package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.example.tundra_snow_app.Models.Users;
import com.google.firebase.firestore.auth.User;

/**
 * UsersTest is a JUnit test class for the Users class.
 * It tests the default constructor, parameterized constructor, and getter and setter methods.
 */
public class UsersTest {
    private Users user, defaultUser;
    private Users organizerUser;

    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        List<String> roles = new ArrayList<>();
        roles.add("user");

        user = new Users(
                "001",
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                "http://example.com/profile.jpg",
                "2000-01-01",
                "1234567890",
                true,
                "device001",
                "Location1",
                roles,
                new ArrayList<>()
        );

        defaultUser = new Users();

        List<String> organizerRoles = new ArrayList<>();
        organizerRoles.add("organizer");

        organizerUser = new Users(
                "002",
                "Alice",
                "Smith",
                "alice.smith@example.com",
                "password456",
                "http://example.com/alice.jpg",
                "1995-05-15",
                "0987654321",
                true,
                "device002",
                "Location2",
                organizerRoles,
                new ArrayList<>()
        );
    }

    /**
     * Test the default constructor.
     */
    @Test
    public void testDefaultUser() {
        assertNotNull(defaultUser);
    }

    /**
     * Test the parameterized constructor.
     */
    @Test
    public void testGetUserID() {
        user.setUserID("002");
        assertEquals("002", user.getUserID());
        user.setUserID("001");
    }

    /**
     * Test the getter method for the firstName field.
     */
    @Test
    public void testGetFirstName() {
        assertEquals("John", user.getFirstName());
    }

    /**
     * Test the getter method for the lastName field.
     */
    @Test
    public void testSetLastName() {
        user.setLastName("Smith");
        assertEquals("Smith", user.getLastName());
    }

    /**
     * Test the setter method for the password field.
     */
    @Test
    public void testSetPassword() {
        user.setPassword("newpassword123");
        assertEquals("newpassword123", user.getPassword());
    }

    /**
     * Test the setter method for the email field.
     */
    @Test
    public void testSetEmail() {
        user.setEmail("new@email.com");
        assertEquals("new@email.com", user.getEmail());
    }

    /**
     * Test the setter method for the dateOfBirth field.
     */
    @Test
    public void testSetDateOfBirth() {
        user.setDateOfBirth("1990-12-31");
        assertEquals("1990-12-31", user.getDateOfBirth());
    }

    /**
     * Test the setter method for the phoneNumber field.
     */
    @Test
    public void testSetPhoneNumber() {
        user.setPhoneNumber("9876543210");
        assertEquals("9876543210", user.getPhoneNumber());
    }

    /**
     * Test the setter method for the deviceId field.
     */
    @Test
    public void testSetDeviceId() {
        user.setDeviceID("device003");
        assertEquals("device003", user.getDeviceID());
    }

    /**
     * Test method for setting the roles field.
     */
    @Test
    public void testSetRoles() {
        List<String> newRoles = new ArrayList<>();
        newRoles.add("admin");
        newRoles.add("organizer");
        user.setRole(newRoles);

        List<String> roles = user.getRoles();
        assertEquals(2, roles.size());
        assertTrue(roles.contains("admin"));
        assertTrue(roles.contains("organizer"));
    }

    /**
     * Test method for setting the roles field with an invalid role.
     */
    @Test
    public void testSetRolesError() {
        List<String> invalidRoles = new ArrayList<>();
        invalidRoles.add("viewer");

        // Expect an IllegalArgumentException when setting an invalid role
        assertThrows(IllegalArgumentException.class, () -> user.setRole(invalidRoles));
    }

    /**
     * Test method for setting the firstName field.
     */
    @Test
    public void testSetFirstName() {
        user.setFirstName("Jane");
        assertEquals("Jane", user.getFirstName());
    }

    /**
     * Test method for getting the roles field.
     */
    @Test
    public void testGetRoles() {
        List<String> roles = user.getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains("user"));
    }

    /**
     * Test permissions belonging to the user role.
     */
    @Test
    public void testAssignPermissionsForUserRole() {
        List<String> permissions = user.getPermissions();
        assertTrue(permissions.contains("JOIN_WAITLIST"));
        assertTrue(permissions.contains("VIEW_EVENT_DETAILS"));
        assertTrue(permissions.contains("UPDATE_PROFILE"));
    }

    /**
     * Test permissions belonging to the organizer role.
     */
    @Test
    public void testAssignPermissionsForOrganizerRole() {
        List<String> permissions = organizerUser.getPermissions();
        assertTrue(permissions.contains("CREATE_EVENT"));
        assertTrue(permissions.contains("VIEW_ENTRANT_LIST"));
        assertTrue(permissions.contains("MANAGE_ENTRANTS"));
        assertTrue(permissions.contains("NOTIFY_ENTRANTS"));
    }

    /**
     * Test permissions belonging to the admin role.
     */
    @Test
    public void testAddRole() {
        user.addRole("admin");
        List<String> roles = user.getRoles();
        assertTrue(roles.contains("admin"));
        List<String> permissions = user.getPermissions();
        assertTrue(permissions.contains("REMOVE_EVENT"));
    }

    /**
     * Test removing a role from the user.
     */
    @Test
    public void testRemoveRole() {
        user.addRole("admin");
        user.removeRole("admin");
        List<String> roles = user.getRoles();
        assertFalse(roles.contains("admin"));
    }

    /**
     * Test adding a new event to the userEventList.
     */
    @Test
    public void testAddUserEvent() {
        user.addUserEvent("event001");
        List<String> userEventList = user.getUserEventList();
        assertTrue(userEventList.contains("event001"));
    }

    /**
     * Test adding a new facility to the facilityList.
     */
    @Test
    public void testAddFacility() {
        organizerUser.addFacility("facility001");
        List<String> facilityList = organizerUser.getFacilityList();
        assertTrue(facilityList.contains("facility001"));
    }

    /**
     * Test adding a new facility to the facilityList without the organizer role.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testAddFacilityWithoutOrganizerRole() {
        user.addFacility("facility001");
        assertFalse(user.getFacilityList().contains("facility001"));
    }

    @Test
    public void testAddOrganizerEvent() {
        organizerUser.addOrganizerEvent("event002");
        List<String> organizerEventList = organizerUser.getOrganizerEventList();
        assertTrue(organizerEventList.contains("event002"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddOrganizerEventWithoutOrganizerRole() {
        user.addOrganizerEvent("event002");
        assertFalse(user.getOrganizerEventList().contains("event002"));
    }

    /**
     * Test setter method for the location field.
     */
    @Test
    public void testSetLocation() {
        user.setLocation("New Location");
        assertEquals("New Location", user.getLocation());
    }

    /**
     * Test setter method for the notificationsEnabled field.
     */
    @Test
    public void testSetNotificationsEnabled() {
        user.setNotificationsEnabled(false);
        assertFalse(user.isNotificationsEnabled());
    }
}
