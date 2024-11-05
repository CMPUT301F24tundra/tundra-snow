package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import com.example.tundra_snow_app.Organizers;

public class OrganizersTest {
    private Organizers organizer;

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
                "device002",
                "Location2",
                roles,
                facilityList
        );
    }

    @Test
    public void testGetUserID() {
        assertEquals("002", organizer.getUserID());
    }

    @Test
    public void testGetFirstName() {
        assertEquals("Alice", organizer.getFirstName());
    }

    @Test
    public void testGetLastName() {
        assertEquals("Smith", organizer.getLastName());
    }

    @Test
    public void testGetEmail() {
        assertEquals("alice.smith@example.com", organizer.getEmail());
    }

    @Test
    public void testGetPhoneNumber() {
        assertEquals("0987654321", organizer.getPhoneNumber());
    }

    @Test
    public void testNotificationsEnabled() {
        assertTrue(organizer.isNotificationsEnabled());
    }

    @Test
    public void testGetLocation() {
        assertEquals("Location2", organizer.getLocation());
    }

    @Test
    public void testGetRoles() {
        List<String> roles = organizer.getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains("organizer"));
    }

    @Test
    public void testGetFacilityList() {
        List<String> facilityList = organizer.getFacilityList();
        assertEquals(1, facilityList.size());
        assertTrue(facilityList.contains("Conference Hall"));
    }

    @Test
    public void testAddFacility() {
        organizer.addFacility("Exhibition Center");
        List<String> facilityList = organizer.getFacilityList();
        assertEquals(2, facilityList.size());
        assertTrue(facilityList.contains("Exhibition Center"));
    }

    @Test
    public void testAddOrganizerEvent() {
        organizer.addOrganizerEvent("event003");
        List<String> organizerEventList = organizer.getOrganizerEventList();
        assertTrue(organizerEventList.contains("event003"));
    }

    @Test
    public void testAssignPermissionsForOrganizerRole() {
        List<String> permissions = organizer.getPermissions();
        assertTrue(permissions.contains("CREATE_EVENT"));
        assertTrue(permissions.contains("VIEW_ENTRANT_LIST"));
        assertTrue(permissions.contains("MANAGE_ENTRANTS"));
        assertTrue(permissions.contains("NOTIFY_ENTRANTS"));
    }
}
