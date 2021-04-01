package com.weebly.explearn.familymap.model;

import com.weebly.explearn.familymap.R;
import com.weebly.explearn.familymap.ui.EventActivity;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;

import dbModels.Event;
import dbModels.Person;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataCacheTest extends TestCase {

    private DataCache dc;
    private Person testPerson1;
    private Person testPerson2;
    private Person testPerson3;
    private Person testPerson4;
    private Person testPerson5;
    private Event testEvent1;
    private Event testEvent2;
    private Event testEvent3;
    private Event testEvent4;
    private Event testEvent5;
    private Event testEvent6;
    private Event testEvent7;

    @BeforeAll
    public void setUp() {
        dc = DataCache.getInstance();
        testPerson1 = new Person("01", "dyl", "Dylan", "Webb",
                "m", "02", "03", "04");
        testPerson2 = new Person("02", "dad", "Father", "Webb",
                "m", null, null, "03");
        testPerson3 = new Person("03", "dyl", "Mother", "Webb",
                "f", null, null,"02");
        testPerson4 = new Person("04", "dyl", "Unknown", "Webb",
                "f", null, null,"01");
        testPerson5 = new Person("05", "dyl", "Evelyn", "Webb",
                "f", "01", "04",null);
        testEvent1 = new Event("01", "dyl", "01",
                24.0, 8.9, "USA", "Provo", "Attended College", 2019);
        testEvent2 = new Event("02", "dyl", "01",
                21.5, 9.4, "USA", "SLC", "Death", 2021);
        testEvent3 = new Event("03", "dyl", "01",
                21.5, 9.4, "USA", "SLC", "Birth", 2019);
        testEvent4 = new Event("04", "dyl", "01",
                24.0, 8.9, "USA", "Provo", "CS240", 2021);
        testEvent5 = new Event("05", "dyl", "01",
                -30.2, 6.6, "Italy", "Rome", "Burial", 2021);
        testEvent6 = new Event("06", "dyl", "04",
                -30.2, 6.6, "Egypt", "Cyrus", "Ate a Cookie", 2024);
        testEvent7 = new Event("07", "dyl", "05",
                -30.2, 6.6, "England", "London", "Birth", 2020);
        ArrayList<Person> people = new ArrayList<>();
        people.add(testPerson1);
        people.add(testPerson2);
        people.add(testPerson3);
        people.add(testPerson4);
        people.add(testPerson5);
        ArrayList<Event> events = new ArrayList<>();
        events.add(testEvent1);
        events.add(testEvent2);
        events.add(testEvent3);
        events.add(testEvent4);
        events.add(testEvent5);
        events.add(testEvent6);
        events.add(testEvent7);
        dc.insertPeople(people);
        dc.insertEvents(events);
    }

    @Test
    public void testPassGetLifeEvents() {
        ArrayList<Event> lifeEvents = dc.getLifeEvents(testPerson1.getPersonID());
        assertEquals(testEvent3.getEventID(), lifeEvents.get(0).getEventID());
        assertEquals(testEvent1.getEventID(), lifeEvents.get(1).getEventID());
        assertEquals(testEvent5.getEventID(), lifeEvents.get(2).getEventID()); // 4 and 5 test
        assertEquals(testEvent4.getEventID(), lifeEvents.get(3).getEventID()); // alphabetical
        assertEquals(testEvent2.getEventID(), lifeEvents.get(4).getEventID());
        assertThrows(IndexOutOfBoundsException.class, ()-> lifeEvents.get(5));

        assertTrue(lifeEvents.get(0).getYear() <= lifeEvents.get(1).getYear());
        assertTrue(lifeEvents.get(1).getYear() <= lifeEvents.get(2).getYear());
        assertTrue(lifeEvents.get(2).getYear() <= lifeEvents.get(3).getYear());
        assertTrue(lifeEvents.get(3).getYear() <= lifeEvents.get(4).getYear());

        // tests birth and death vs alphabetical order precedence
        assertEquals("Birth", lifeEvents.get(0).getEventType());
        assertEquals("Death", lifeEvents.get(4).getEventType());
    }

    @Test
    public void testFailGetLifeEvents() {
        ArrayList<Event> lifeEvents = dc.getLifeEvents(testPerson3.getPersonID());
        assertTrue(lifeEvents.isEmpty());

        lifeEvents = dc.getLifeEvents(null);
        assertTrue(lifeEvents.isEmpty());

        assertThrows(NullPointerException.class, ()-> dc.insertEvents(null));
    }

    @Test
    public void testPassGetFamily() {
        ArrayList<Person> family = dc.getFamily(testPerson1.getPersonID());
        assertEquals(testPerson2.getPersonID(), family.get(0).getPersonID());
        assertEquals(testPerson3.getPersonID(), family.get(1).getPersonID());
        assertEquals(testPerson4.getPersonID(), family.get(2).getPersonID());
        assertEquals(testPerson5.getPersonID(), family.get(3).getPersonID());
        assertThrows(IndexOutOfBoundsException.class, ()-> family.get(4));

        assertEquals(testPerson1.getFatherID(), family.get(0).getPersonID());
        assertEquals(testPerson1.getMotherID(), family.get(1).getPersonID());
        assertEquals(testPerson1.getSpouseID(), family.get(2).getPersonID());
        assertEquals(testPerson1.getPersonID(), family.get(3).getFatherID());
        assertFalse(family.contains(testPerson1));
    }

    @Test
    public void testFailGetFamily() {
        Person testPerson = new Person("07", "weird0", "who", "dis",
                "m", null, null, null);
        ArrayList<Person> family = dc.getFamily(testPerson.getPersonID());
        assertTrue(family.isEmpty());

        family = dc.getFamily(null);
        assertTrue(family.isEmpty());

        assertThrows(NullPointerException.class, ()-> dc.insertPeople(null));
    }

    @Test
    public void testPassGetSearchedPeople() {
        ArrayList<Person> people = dc.getSearchedPeople("d");
        assertFalse(people.isEmpty());
        assertEquals(1, people.size());
        ArrayList<Person> newPeople = dc.getSearchedPeople("D");
        assertEquals(people.get(0), newPeople.get(0));

        people = dc.getSearchedPeople("ther");
        assertEquals(2, people.size());
        people = dc.getSearchedPeople("other");
        assertEquals(1, people.size());


        people = dc.getSearchedPeople("webb");
        assertEquals(dc.getPeople().size(), people.size());
    }

    @Test
    public void testFailGetSearchedPeople() {
        ArrayList<Person> people = dc.getSearchedPeople("");
        assertTrue(people.isEmpty());

        people = dc.getSearchedPeople("funky");
        assertTrue(people.isEmpty());

        assertThrows(NullPointerException.class, ()-> dc.getSearchedPeople(null));
    }

    @Test
    public void testPassGetSearchedEvents() {
        ArrayList<Event> events = dc.getSearchedEvents("rome");
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        ArrayList<Event> newEvents = dc.getSearchedEvents("ROME");
        assertEquals(events.get(0), newEvents.get(0));

        events = dc.getSearchedEvents("usa");
        assertEquals(4, events.size());
        events = dc.getSearchedEvents("2021");
        assertEquals(3, events.size());
        events = dc.getSearchedEvents("202");
        assertEquals(5, events.size());
        events = dc.getSearchedEvents("slc");
        assertEquals(2, events.size());


        events = dc.getSearchedEvents("webb");
        assertEquals(dc.getEvents().size(), events.size());
    }

    @Test
    public void testFailGetSearchedEvents() {
        ArrayList<Event> events = dc.getSearchedEvents("");
        assertTrue(events.isEmpty());

        events = dc.getSearchedEvents("skydiving");
        assertTrue(events.isEmpty());

        assertThrows(NullPointerException.class, ()-> dc.getSearchedEvents(null));
    }

    @Test
    public void testPassGetEventFilters() {
        dc.setUser(testPerson5);
        dc.changeEventFilters("father", false);
        assertEquals(2, dc.getEvents().size());
        assertNotNull(dc.getEvents().get(testEvent6.getEventID()));

        dc.changeEventFilters("mother", false);
        assertEquals(1, dc.getEvents().size());
        assertNotNull(dc.getEvents().get(testEvent7.getEventID()));

        dc.changeEventFilters("father", true);
        assertEquals(6, dc.getEvents().size());
        assertNotNull(dc.getEvents().get(testEvent1.getEventID()));

        dc.changeEventFilters("mother", true);
        dc.changeEventFilters("male", false);
        assertEquals(2, dc.getEvents().size());
        assertNotNull(dc.getEvents().get(testEvent6.getEventID()));

        dc.changeEventFilters("male", true);;
    }

    @Test
    public void testAbnormalGetEventFilters() {
        dc.setUser(testPerson1);
        dc.changeEventFilters("father", false);
        dc.changeEventFilters("mother", false);
        dc.changeEventFilters("male", false);
        dc.changeEventFilters("female", false);
        assertTrue(dc.getEvents().isEmpty());

        //checks that spouse event is still present with parent's sides removed
        dc.changeEventFilters("female", true);
        assertEquals(1, dc.getEvents().size());
        assertNull(dc.getEvents().get(testEvent1.getEventID()));
        assertNotNull(dc.getEvents().get(testEvent6.getEventID()));

        dc.changeEventFilters("male", true);
        dc.changeEventFilters("father", true);
        dc.changeEventFilters("mother", true);
    }
}