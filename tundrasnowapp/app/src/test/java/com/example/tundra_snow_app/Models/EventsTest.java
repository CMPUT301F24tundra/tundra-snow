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

import com.example.tundra_snow_app.Models.Events;

public class EventsTest {
    private Events event, defaultEvent;
    private SimpleDateFormat dateFormat;
    private Date startDate, endDate, regStartDate, regEndDate;
    private List<String> entrants, confirmed, declined, cancelled, chosen;

    @Before
    public void setUp() throws ParseException {
        // Initialize dates
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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

        defaultEvent = new Events();
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
    public void testDefaultConstructor() {
        Events defaultEvent = new Events();
        assertNotNull(defaultEvent);
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
    public void testSetEntrantList() {
        // Add a single user to the entrant list
        event.addEntrant("User001");
        assertTrue(event.getEntrantList().contains("User001"));

        // Test setting a new entrant list
        List<String> newEntrantList = new ArrayList<>();
        newEntrantList.add("User006");
        newEntrantList.add("User007");
        event.setEntrantList(newEntrantList);

        assertEquals(2, event.getEntrantList().size());
        assertTrue(event.getEntrantList().contains("User006"));
        assertTrue(event.getEntrantList().contains("User007"));
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
    public void testSetConfirmed() {
        // Add a single user to the confirmed list
        event.addConfirmed("User002");
        assertTrue(event.getConfirmedList().contains("User002"));

        // Test setting a new confirmed list
        List<String> newConfirmedList = new ArrayList<>();
        newConfirmedList.add("User003");
        newConfirmedList.add("User004");
        event.setConfirmedList(newConfirmedList);

        assertEquals(2, event.getConfirmedList().size());
        assertTrue(event.getConfirmedList().contains("User003"));
        assertTrue(event.getConfirmedList().contains("User004"));
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
    public void testSetDeclinedList() {
        // Add a single user to the declined list
        event.addDeclined("User003");
        assertTrue(event.getDeclinedList().contains("User003"));

        // Test setting a new declined list
        List<String> newDeclinedList = new ArrayList<>();
        newDeclinedList.add("User008");
        newDeclinedList.add("User009");
        event.setDeclinedList(newDeclinedList);

        assertEquals(2, event.getDeclinedList().size());
        assertTrue(event.getDeclinedList().contains("User008"));
        assertTrue(event.getDeclinedList().contains("User009"));
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
    public void testSetCancelledList() {
        // Add a single user to the cancelled list
        event.addCancelled("User004");
        assertTrue(event.getCancelledList().contains("User004"));

        // Test setting a new cancelled list
        List<String> newCancelledList = new ArrayList<>();
        newCancelledList.add("User010");
        newCancelledList.add("User011");
        event.setCancelledList(newCancelledList);

        assertEquals(2, event.getCancelledList().size());
        assertTrue(event.getCancelledList().contains("User010"));
        assertTrue(event.getCancelledList().contains("User011"));
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
    public void testSetChosenList() {
        // Add a single user to the chosen list
        event.addChosen("User005");
        assertTrue(event.getChosenList().contains("User005"));

        // Test setting a new chosen list
        List<String> newChosenList = new ArrayList<>();
        newChosenList.add("User012");
        newChosenList.add("User013");
        event.setChosenList(newChosenList);

        assertEquals(2, event.getChosenList().size());
        assertTrue(event.getChosenList().contains("User012"));
        assertTrue(event.getChosenList().contains("User013"));
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


    @Test
    public void testSetAndGetEventID() {
        event.setEventID("E002");
        assertEquals("E002", event.getEventID());
    }

    @Test
    public void testSetAndGetOrganizer() {
        event.setOrganizer("O002");
        assertEquals("O002", event.getOrganizer());
    }

    @Test
    public void testSetAndGetTitle() {
        event.setTitle("Summer Festival");
        assertEquals("Summer Festival", event.getTitle());
    }

    @Test
    public void testSetAndGetDescription() {
        event.setDescription("A summer festival event");
        assertEquals("A summer festival event", event.getDescription());
    }

    @Test
    public void testSetAndGetPosterImageURL() {
        event.setPosterImageURL("http://example.com/newposter.jpg");
        assertEquals("http://example.com/newposter.jpg", event.getPosterImageURL());
    }

    @Test
    public void testSetAndGetPublished() {
        event.setPublished("Draft");
        assertEquals("Draft", event.getPublished());
    }

    @Test
    public void testSetAndGetQrHash() {
        event.setQrHash("NEWQR123");
        assertEquals("NEWQR123", event.getQrHash());
    }

    @Test
    public void testFormattedDateWithNullValue() {
        Events eventWithNullDates = new Events();
        assertEquals("Date TBD", eventWithNullDates.getFormattedDate(null));
    }

    @Test
    public void testCapacityBoundary() {
        event.setCapacity(0);
        assertEquals(0, event.getCapacity());

        event.setCapacity(5000);
        assertEquals(5000, event.getCapacity());
    }

    @Test
    public void testSetAndGetStartDate() throws ParseException {
        assertEquals(startDate, event.getStartDate());

        Date newStartDate = dateFormat.parse("2024-12-05");
        event.setStartDate(newStartDate);
        assertEquals(newStartDate, event.getStartDate());
    }

    @Test
    public void testSetAndGetEndDate() throws ParseException {
        assertEquals(endDate, event.getEndDate());

        Date newEndDate = dateFormat.parse("2024-12-15");
        event.setEndDate(newEndDate);
        assertEquals(newEndDate, event.getEndDate());
    }

    @Test
    public void testSetAndGetRegistrationStartDate() throws ParseException {
        assertEquals(regStartDate, event.getRegistrationStartDate());

        Date newRegStartDate = dateFormat.parse("2024-11-05");
        event.setRegistrationStartDate(newRegStartDate);
        assertEquals(newRegStartDate, event.getRegistrationStartDate());
    }

    @Test
    public void testSetAndGetRegistrationEndDate() throws ParseException {
        assertEquals(regEndDate, event.getRegistrationEndDate());

        Date newRegEndDate = dateFormat.parse("2024-11-25");
        event.setRegistrationEndDate(newRegEndDate);
        assertEquals(newRegEndDate, event.getRegistrationEndDate());
    }

    @Test
    public void testFormattedStartDate() {
        // Verify formatted start date
        SimpleDateFormat expectedFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        assertEquals(expectedFormat.format(startDate), event.getFormattedDateStart());
    }

    @Test
    public void testFormattedEndDate() {
        // Verify formatted end date
        SimpleDateFormat expectedFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        assertEquals(expectedFormat.format(endDate), event.getFormattedDateEnd());
    }

    @Test
    public void testFormattedRegistrationStartDate() {
        // Verify formatted registration start date
        SimpleDateFormat expectedFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        assertEquals(expectedFormat.format(regStartDate), event.getFormattedRegStart());
    }

    @Test
    public void testFormattedRegistrationEndDate() {
        // Verify formatted registration end date
        SimpleDateFormat expectedFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        assertEquals(expectedFormat.format(regEndDate), event.getFormattedRegDateEnd());
    }

    @Test
    public void testFormattedDateWithNull() {
        // Set a null date and check that the formatted output is "Date TBD"
        assertEquals("Date TBD", event.getFormattedDate(null));
    }

    @Test
    public void testFormattedDateWithFutureDate() throws ParseException {
        // Test with a future date (e.g., Jan 1, 2100)
        Date futureDate = dateFormat.parse("2100-01-01");
        SimpleDateFormat expectedFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        assertEquals(expectedFormat.format(futureDate), event.getFormattedDate(futureDate));
    }
}
