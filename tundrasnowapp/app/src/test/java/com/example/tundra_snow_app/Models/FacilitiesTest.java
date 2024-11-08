package com.example.tundra_snow_app.Models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.tundra_snow_app.Models.Facilities;

/**
 * FacilitiesTest is a JUnit test class for the Facilities class.
 * It tests the default constructor, parameterized constructor, and getter and setter methods.
 */
public class FacilitiesTest {
    private Facilities facility;

    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        facility = new Facilities("Main Hall", "Downtown", "F001");
    }

    /**
     * Test the default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        Facilities emptyFacility = new Facilities();
        assertNull(emptyFacility.getFacilityName());
        assertNull(emptyFacility.getFacilityLocation());
        assertNull(emptyFacility.getFacilityID());
    }

    /**
     * Test the parameterized constructor.
     */
    @Test
    public void testParameterizedConstructor() {
        assertEquals("Main Hall", facility.getFacilityName());
        assertEquals("Downtown", facility.getFacilityLocation());
        assertEquals("F001", facility.getFacilityID());
    }

    /**
     * Test getter method for the facilityName field.
     */
    @Test
    public void testGetFacilityName() {
        assertEquals("Main Hall", facility.getFacilityName());
    }

    /**
     * Test setter method for the facilityName field.
     */
    @Test
    public void testSetFacilityName() {
        facility.setFacilityName("Conference Room");
        assertEquals("Conference Room", facility.getFacilityName());
    }

    /**
     * Test getter method for the facilityLocation field.
     */
    @Test
    public void testGetFacilityLocation() {
        assertEquals("Downtown", facility.getFacilityLocation());
    }

    /**
     * Test setter method for the facilityLocation field.
     */
    @Test
    public void testSetFacilityLocation() {
        facility.setFacilityLocation("Uptown");
        assertEquals("Uptown", facility.getFacilityLocation());
    }

    /**
     * Test getter method for the facilityID field.
     */
    @Test
    public void testGetFacilityID() {
        assertEquals("F001", facility.getFacilityID());
    }

    /**
     * Test setter method for the facilityID field.
     */
    @Test
    public void testSetFacilityID() {
        facility.setFacilityID("F002");
        assertEquals("F002", facility.getFacilityID());
    }
}
