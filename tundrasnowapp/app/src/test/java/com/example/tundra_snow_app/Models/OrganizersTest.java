package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.example.tundra_snow_app.Models.Organizers;

/**
 * OrganizersTest is a JUnit test class for the Organizers class.
 * It tests the default constructor, parameterized constructor, and getter and setter methods.
 */
public class OrganizersTest {
    private Organizers organizer;

    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        List<String> roles = new ArrayList<>();
        roles.add("organizer");

        List<String> facilityList = new ArrayList<>();
        facilityList.add("Conference Hall");

        organizer = new Organizers(
                "002",
                "Alice",
                "Smith",
                "alice.smith@example.com",
                "password456",
                "http://example.com/alice.jpg",
                "1995-05-15",
                "0987654321",
                true,
                true,
                "device002",
                "Location2",
                roles,
                facilityList
        );
    }
    
    /**
     * Test getter method for the userID field.
     */
    @Test
    public void testGetUserID() {
        assertEquals("002", organizer.getUserID());
    }

    /**
     * Test getter method for the firstName field.
     */
    @Test
    public void testGetFirstName() {
        assertEquals("Alice", organizer.getFirstName());
    }

    /**
     * Test getter method for the lastName field.
     */
    @Test
    public void testGetLastName() {
        assertEquals("Smith", organizer.getLastName());
    }

    /**
     * Test getter method for the email field.
     */
    @Test
    public void testGetEmail() {
        assertEquals("alice.smith@example.com", organizer.getEmail());
    }

    /**
     * Test getter method for the password field.
     */
    @Test
    public void testGetPhoneNumber() {
        assertEquals("0987654321", organizer.getPhoneNumber());
    }

    /**
     * Test getter method for notificationsEnabled field.
     */
    @Test
    public void testNotificationsEnabled() {
        assertTrue(organizer.isNotificationsEnabled());
    }

    /**
     * Test getter method for the location field.
     */
    @Test
    public void testGetLocation() {
        assertEquals("Location2", organizer.getLocation());
    }

    /**
     * Test getter method for the roles field.
     */
    @Test
    public void testGetRoles() {
        List<String> roles = organizer.getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains("organizer"));
    }

    /**
     * Test getter method for the facilityList field.
     */
    @Test
    public void testGetFacilityList() {
        List<String> facilityList = organizer.getFacilityList();
        assertEquals(1, facilityList.size());
        assertTrue(facilityList.contains("Conference Hall"));
    }

    /**
     * Test adding a new facility to the facilityList.
     */
    @Test
    public void testAddFacility() {
        organizer.addFacility("Exhibition Center");
        List<String> facilityList = organizer.getFacilityList();
        assertEquals(2, facilityList.size());
        assertTrue(facilityList.contains("Exhibition Center"));
    }

    /**
     * Test adding a new event to the organizerEventList.
     */
    @Test
    public void testAddOrganizerEvent() {
        organizer.addOrganizerEvent("event003");
        List<String> organizerEventList = organizer.getOrganizerEventList();
        assertTrue(organizerEventList.contains("event003"));
    }

    /** 
     * Test assigning permissions for the organizer role.
     */
    @Test
    public void testAssignPermissionsForOrganizerRole() {
        List<String> permissions = organizer.getPermissions();
        assertTrue(permissions.contains("CREATE_EVENT"));
        assertTrue(permissions.contains("VIEW_ENTRANT_LIST"));
        assertTrue(permissions.contains("MANAGE_ENTRANTS"));
        assertTrue(permissions.contains("NOTIFY_ENTRANTS"));
    }

    /**
     * Test for checking if email is in valid form
     */
    @Test
    public void testInvalidEmail() {
        try {
            organizer.setEmail("invalid-email");
            fail("Exception not thrown for invalid email");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid email format", e.getMessage());
        }
    }

    /**
     * Test for checking init on facilities
     */
    @Test
    public void testEmptyFacilityList() {
        organizer = new Organizers(
                "003",
                "Bob",
                "Brown",
                "bob.brown@example.com",
                "password789",
                "",
                "",
                "",
                false,
                false,
                "device003",
                "Location3",
                new ArrayList<>(),
                new ArrayList<>()
        );
        organizer.addRole("organizer");
        List<String> facilityList = organizer.getFacilityList();
        assertNull(facilityList);
    }
}
