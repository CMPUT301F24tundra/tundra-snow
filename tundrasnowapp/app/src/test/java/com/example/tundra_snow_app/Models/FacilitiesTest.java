package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.tundra_snow_app.Facilities;

public class FacilitiesTest {
    private Facilities facility;

    @Before
    public void setUp() {
        facility = new Facilities("Main Hall", "Downtown", "F001");
    }

    @Test
    public void testDefaultConstructor() {
        Facilities emptyFacility = new Facilities();
        assertNull(emptyFacility.getFacilityName());
        assertNull(emptyFacility.getFacilityLocation());
        assertNull(emptyFacility.getFacilityID());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("Main Hall", facility.getFacilityName());
        assertEquals("Downtown", facility.getFacilityLocation());
        assertEquals("F001", facility.getFacilityID());
    }

    @Test
    public void testGetFacilityName() {
        assertEquals("Main Hall", facility.getFacilityName());
    }

    @Test
    public void testSetFacilityName() {
        facility.setFacilityName("Conference Room");
        assertEquals("Conference Room", facility.getFacilityName());
    }

    @Test
    public void testGetFacilityLocation() {
        assertEquals("Downtown", facility.getFacilityLocation());
    }

    @Test
    public void testSetFacilityLocation() {
        facility.setFacilityLocation("Uptown");
        assertEquals("Uptown", facility.getFacilityLocation());
    }

    @Test
    public void testGetFacilityID() {
        assertEquals("F001", facility.getFacilityID());
    }

    @Test
    public void testSetFacilityID() {
        facility.setFacilityID("F002");
        assertEquals("F002", facility.getFacilityID());
    }
}
