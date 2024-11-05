package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

import com.example.tundra_snow_app.Events;

public class EventsTest {
    private Events event;
    private Date startDate, endDate, regStartDate, regEndDate;
    private List<String> entrants, confirmed, declined, cancelled, chosen;

    @Before
    public void setUp() throws ParseException {
        // Initialize dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = dateFormat.parse("2024-12-01");
        endDate = dateFormat.parse("2024-12-10");
        regStartDate = dateFormat.parse("2024-11-01");
        regEndDate = dateFormat.parse("2024-11-30");

        // Initialize lists
        entrants = new ArrayList<>();
        confirmed = new ArrayList<>();
        declined = new ArrayList<>();
        cancelled = new ArrayList<>();
        chosen = new ArrayList<>();

        event = new Events(
                "E001",
                "O001",
                "Winter Festival",
                "A festive winter event",
                "http://example.com/poster.jpg",
                "City Park",
                "Published",
                startDate,
                endDate,
                regEndDate,
                regStartDate,
                100,
                "QR123",
                "Scheduled",
                confirmed,
                declined,
                entrants,
                cancelled,
                chosen
        );
    }

    @Test
    public void testConstructor() {
        assertEquals("E001", event.getEventID());
        assertEquals("O001", event.getOrganizer());
        assertEquals("Winter Festival", event.getTitle());
        assertEquals("A festive winter event", event.getDescription());
        assertEquals("http://example.com/poster.jpg", event.getPosterImageURL());
        assertEquals("City Park", event.getLocation());
        assertEquals("Published", event.getPublished());
        assertEquals("QR123", event.getQrHash());
        assertEquals("Scheduled", event.getStatus());
        assertEquals(100, event.getCapacity());
    }

    @Test
    public void testDateFormatting() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());

        assertEquals(dateFormat.format(startDate), event.getFormattedDateStart());
        assertEquals(dateFormat.format(endDate), event.getFormattedDateEnd());
        assertEquals(dateFormat.format(regStartDate), event.getFormattedRegStart());
        assertEquals(dateFormat.format(regEndDate), event.getFormattedRegDateEnd());
    }

    @Test
    public void testAddAndRemoveEntrant() {
        event.addEntrant("User001");
        assertTrue(event.getEntrantList().contains("User001"));
        event.removeEntrant("User001");
        assertFalse(event.getEntrantList().contains("User001"));
    }

    @Test
    public void testAddAndRemoveConfirmed() {
        event.addConfirmed("User002");
        assertTrue(event.getConfirmedList().contains("User002"));
        event.removeConfirmed("User002");
        assertFalse(event.getConfirmedList().contains("User002"));
    }

    @Test
    public void testAddAndRemoveDeclined() {
        event.addDeclined("User003");
        assertTrue(event.getDeclinedList().contains("User003"));
        event.removeDeclined("User003");
        assertFalse(event.getDeclinedList().contains("User003"));
    }

    @Test
    public void testAddAndRemoveCancelled() {
        event.addCancelled("User004");
        assertTrue(event.getCancelledList().contains("User004"));
        event.removeCancelled("User004");
        assertFalse(event.getCancelledList().contains("User004"));
    }

    @Test
    public void testAddAndRemoveChosen() {
        event.addChosen("User005");
        assertTrue(event.getChosenList().contains("User005"));
        event.removeChosen("User005");
        assertFalse(event.getChosenList().contains("User005"));
    }

    @Test
    public void testSetCapacity() {
        event.setCapacity(150);
        assertEquals(150, event.getCapacity());
    }

    @Test
    public void testSetAndGetLocation() {
        event.setLocation("New Venue");
        assertEquals("New Venue", event.getLocation());
    }

    @Test
    public void testSetAndGetStatus() {
        event.setStatus("Cancelled");
        assertEquals("Cancelled", event.getStatus());
    }
}
