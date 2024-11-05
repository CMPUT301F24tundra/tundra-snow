package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.example.tundra_snow_app.Users;

public class UsersTest {
    private Users user;
    private Users organizerUser;

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

    @Test
    public void testGetUserID() {
        assertEquals("001", user.getUserID());
    }

    @Test
    public void testGetFirstName() {
        assertEquals("John", user.getFirstName());
    }

    @Test
    public void testSetFirstName() {
        user.setFirstName("Jane");
        assertEquals("Jane", user.getFirstName());
    }

    @Test
    public void testGetRoles() {
        List<String> roles = user.getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains("user"));
    }

    @Test
    public void testAssignPermissionsForUserRole() {
        List<String> permissions = user.getPermissions();
        assertTrue(permissions.contains("JOIN_WAITLIST"));
        assertTrue(permissions.contains("VIEW_EVENT_DETAILS"));
        assertTrue(permissions.contains("UPDATE_PROFILE"));
    }

    @Test
    public void testAssignPermissionsForOrganizerRole() {
        List<String> permissions = organizerUser.getPermissions();
        assertTrue(permissions.contains("CREATE_EVENT"));
        assertTrue(permissions.contains("VIEW_ENTRANT_LIST"));
        assertTrue(permissions.contains("MANAGE_ENTRANTS"));
        assertTrue(permissions.contains("NOTIFY_ENTRANTS"));
    }

    @Test
    public void testAddRole() {
        user.addRole("admin");
        List<String> roles = user.getRoles();
        assertTrue(roles.contains("admin"));
        List<String> permissions = user.getPermissions();
        assertTrue(permissions.contains("REMOVE_EVENT"));
    }

    @Test
    public void testRemoveRole() {
        user.addRole("admin");
        user.removeRole("admin");
        List<String> roles = user.getRoles();
        assertFalse(roles.contains("admin"));
    }

    @Test
    public void testAddUserEvent() {
        user.addUserEvent("event001");
        List<String> userEventList = user.getUserEventList();
        assertTrue(userEventList.contains("event001"));
    }

    @Test
    public void testAddFacility() {
        organizerUser.addFacility("facility001");
        List<String> facilityList = organizerUser.getFacilityList();
        assertTrue(facilityList.contains("facility001"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddFacilityWithoutOrganizerRole() {
        user.addFacility("facility001");
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
    }

    @Test
    public void testSetLocation() {
        user.setLocation("New Location");
        assertEquals("New Location", user.getLocation());
    }

    @Test
    public void testSetNotificationsEnabled() {
        user.setNotificationsEnabled(false);
        assertFalse(user.isNotificationsEnabled());
    }
}
