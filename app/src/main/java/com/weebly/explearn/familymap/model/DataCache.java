package com.weebly.explearn.familymap.model;

import com.weebly.explearn.familymap.ui.PersonActivity;

import java.util.*;
import dbModels.*;

/**
 * A singleton class which holds all the person and event data for the family tree
 */
public class DataCache {

    private static DataCache instance;

    /**
     * When first called, a new instance of the datacache is created
     * The same instance is shared among all classes
     *
     * @return the current instance of the datacache
     */
    public static DataCache getInstance() {

        if(instance == null) {
            instance = new DataCache();
        }

        return instance;
    }

    private Person user; // the current user

    private final ArrayList<String> eventFilters; // current filters on events
    private final ArrayList<String> lineFilters; // current filters on lines

    private final Map<String, Person> mapPeople; // people whose events appear on the map
    private final Map<String, Event> mapEvents; // events which appear on the map
    private final Map<String, Person> allPeople; // all people
    private final Map<String, Event> allEvents; // all events
    private final Map<String, String> colorMap; // a mapping of event/line type to color

    private boolean loggedIn; // holds whether or not the user is logged in
    private boolean updatedEvents; // holds whether or not event filters have been updated
    private boolean updatedLines; // holds whether or not line filters have been updated

    private DataCache() {
        user = null;

        eventFilters = new ArrayList<>();
        eventFilters.add("father");
        eventFilters.add("mother");
        eventFilters.add("male");
        eventFilters.add("female");

        lineFilters = new ArrayList<>();
        lineFilters.add("life_story");
        lineFilters.add("family_tree");
        lineFilters.add("spouse");

        mapPeople = new HashMap<>();
        mapEvents = new HashMap<>();
        allPeople = new HashMap<>();
        allEvents = new HashMap<>();
        colorMap = new HashMap<>();

        loggedIn = false;
        updatedEvents = true;
        updatedLines = true;
    }

    public Map<String, Person> getPeople() {
        return allPeople;
    }

    public Map<String, Event> getEvents() {
        return mapEvents;
    }

    public void setUser(Person person) {
        user = person;
    }

    public ArrayList<String> getEventFilters() {
        return eventFilters;
    }

    public ArrayList<String> getLineFilters() {
        return lineFilters;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isUpdatedEvents() {
        return updatedEvents;
    }

    public boolean isUpdatedLines() {
        return updatedLines;
    }

    /**
     * Inserts person objects into allPeaople and mapPeople to be stored
     * 
     * @param peopleToInsert an array of person objects to be inserted
     */
    public void insertPeople(ArrayList<Person> peopleToInsert) {
        for (Person person: peopleToInsert) {
            allPeople.put(person.getPersonID(), person);
            mapPeople.put(person.getPersonID(), person);
        }
    }

    /**
     * Inserts event objects into allEvents and mapEvents to be stored
     *
     * @param eventsToInsert an array of event objects to be inserted
     */
    public void insertEvents(ArrayList<Event> eventsToInsert) {
        for (Event event: eventsToInsert) {
            allEvents.put(event.getEventID(), event);
            mapEvents.put(event.getEventID(), event);
        }
    }

    /**
     * Puts a person's full name in a single string for quick display
     *
     * @param personID the ID of the queried person
     * @return the first and last name concatenated with a space
     */
    public String getPersonFullName(String personID) {
        Person person = allPeople.get(personID);
        if (person != null) {
            return person.getFirstName() + " " + person.getLastName();
        }
        else {
            return "null";
        }
    }

    /**
     * Puts an event's details in a single string for quick display
     *
     * @param eventID the ID of the queried event
     * @return the type, city, country and year concatenated with punctuation
     */
    public String getEventDetails(String eventID) {
        Event event = allEvents.get(eventID);
        if (event != null) {
            return event.getEventType() + ": " + event.getCity() + ", " +
                    event.getCountry() + " (" + event.getYear() + ")";
        }
        else {
            return "null";
        }
    }

    /**
     * Retrieves all life events of a given person in chronological order
     *
     * @param personID the ID of the queried person
     * @return a chronologically ordered list of event objects
     */
    public ArrayList<Event> getLifeEvents(String personID) {
        ArrayList<Event> lifeEvents = new ArrayList<>();
        for (Event event : mapEvents.values()) {
            if (event.getPersonID().equals(personID)) {
                if (lifeEvents.isEmpty()) { // add first event
                    lifeEvents.add(0, event);
                }
                else {
                    // add subsequent events in chronological order
                    // with birth always first and death always last
                    for (int i = 0; i < lifeEvents.size(); i++) {
                        if (event.getYear() < lifeEvents.get(i).getYear()
                                || event.getEventType().toLowerCase().equals("birth")
                                || lifeEvents.get(i).getEventType().toLowerCase().equals("death")) {
                            lifeEvents.add(i, event);
                            break;
                        }
                        else if (event.getYear() == lifeEvents.get(i).getYear()
                                && !event.getEventType().toLowerCase().equals("death")) {
                            if (event.getEventType().toLowerCase().compareTo(
                                    lifeEvents.get(i).getEventType().toLowerCase()) < 0) {
                                lifeEvents.add(i, event);
                                break;
                            }
                        }
                        else if (i == lifeEvents.size() - 1) {
                            lifeEvents.add(event);
                            break;
                        }
                    }
                }
            }
        }
        return lifeEvents;
    }

    /**
     * Retrieves all family members of a given person
     *
     * @param personID the ID of the queried person
     * @return a list of person objects
     */
    public ArrayList<Person> getFamily(String personID) {
        ArrayList<Person> family = new ArrayList<>();
        Person person = allPeople.get(personID);
        if (person != null) {
            if (person.getFatherID() != null) {
                family.add(allPeople.get(person.getFatherID()));
            }
            if (person.getMotherID() != null) {
                family.add(allPeople.get(person.getMotherID()));
            }
            if (person.getSpouseID() != null) {
                family.add(allPeople.get(person.getSpouseID()));
            }
            for (Person child : allPeople.values()) {
                if (child.getMotherID() != null) {
                    if (child.getMotherID().equals(personID)) {
                        family.add(child);
                    }
                }
                if (child.getFatherID() != null) {
                    if (child.getFatherID().equals(personID)) {
                        family.add(child);
                    }
                }
            }
        }
        return family;
    }

    /**
     * Filters allPeople by a given search string
     *
     * @param search the string to be queried
     * @return a list of person objects
     */
    public ArrayList<Person> getSearchedPeople(String search) {
        ArrayList<Person> filteredPeople = new ArrayList<>();
        if (!search.isEmpty()) {
            for (Person person : allPeople.values()) {
                if ((person.getFirstName().toLowerCase().contains(search.toLowerCase()) ||
                        person.getLastName().toLowerCase().contains(search.toLowerCase()))) {
                    filteredPeople.add(person);
                }
            }
        }
        return filteredPeople;
    }

    /**
     * Filters mapEvents by a given search string
     *
     * @param search the string to be queried
     * @return a list of event objects
     */
    public ArrayList<Event> getSearchedEvents(String search) {
        ArrayList<Event> filteredEvents = new ArrayList<>();
        if (!search.isEmpty()) {
            for (Event event : mapEvents.values()) {
                if (event.getEventType().toLowerCase().contains(search.toLowerCase()) ||
                        event.getCountry().toLowerCase().contains(search.toLowerCase()) ||
                        event.getCity().toLowerCase().contains(search.toLowerCase()) ||
                        String.valueOf(event.getYear()).contains(search.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }
        }
        return filteredEvents;
    }

    /**
     * Adds and removes event filters when toggled in the settings activity
     * Calls functions which enact filter changes in mapPeople and mapEvents
     *
     * @param filter the filter to be added or removed from eventFilters
     * @param add a boolean defining the operation to be performed
     */
    public void changeEventFilters(String filter, boolean add) {
        updatedEvents = false;
        if (add) {
            eventFilters.add(filter);
        }
        else {
            eventFilters.remove(filter);
        }
        filterMapEvents();
    }

    /**
     * Adds and removes line filters when toggled in the settings activity
     *
     * @param filter the filter to be added or removed from lineFilters
     * @param add a boolean defining the operation to be performed
     */
    public void changeLineFilters(String filter, boolean add) {
        updatedLines = false;
        if (add) {
            lineFilters.add(filter);
        }
        else {
            lineFilters.remove(filter);
        }
    }

    /**
     * Resets the values of updatedLines and updatedEvents after
     * the map fragment confirms they have been updated
     */
    public void confirmUpdate() {
        updatedLines = true;
        updatedEvents = true;
    }

    /**
     * Retrieves the color associated with a specific type of event or line
     * If no associated color exists, a new mapping is defined and stored
     *
     * @param type the associated type of the queried event or line (e.g. male or spouse)
     * @return the name of the corresponding color as found in colors.xml
     */
    public String getCorrespondingColor(String type) {
        final int numColors = 27;
        if (!colorMap.containsKey(type.toLowerCase())) {
            colorMap.put(type.toLowerCase(), "color" + ((colorMap.size() % numColors) + 1));
        }
        return colorMap.get(type.toLowerCase());
    }

    /**
     * Logs the user in
     */
    public void login() {
        loggedIn = true;
    }

    /**
     * Logs the user out and resets the datacache
     */
    public void logout() {
        loggedIn = false;
        updatedEvents = true;
        updatedLines = true;

        allPeople.clear();
        allEvents.clear();
        mapEvents.clear();
        mapPeople.clear();

        eventFilters.clear();
        eventFilters.add("father");
        eventFilters.add("mother");
        eventFilters.add("male");
        eventFilters.add("female");

        lineFilters.clear();
        lineFilters.add("life_story");
        lineFilters.add("family_tree");
        lineFilters.add("spouse");
    }

    private void filterMapEvents() {
        mapPeople.clear();
        mapPeople.put(user.getPersonID(), user);
        if (user.getSpouseID() != null) {
            mapPeople.put(user.getSpouseID(), allPeople.get(user.getSpouseID()));
        }
        mapEvents.clear();
        if (eventFilters.contains("father")) {
            filterAncestors(user.getFatherID());
        }
        if (eventFilters.contains("mother")) {
            filterAncestors(user.getMotherID());
        }
        if (eventFilters.contains("male") && !eventFilters.contains("female")) {
            filterGender("m");
        }
        else if (!eventFilters.contains("male") && eventFilters.contains("female")) {
            filterGender("f");
        }
        else if (!eventFilters.contains("male") && !eventFilters.contains("female")) {
            mapPeople.clear();
        }
        for (Event event : allEvents.values()) {
            if (mapPeople.containsKey(event.getPersonID())) {
                mapEvents.put(event.getEventID(), event);
            }
        }
    }

    private void filterGender(String g) {
        Map<String, Person> tempPeople = new HashMap<>();
        for (Person person : mapPeople.values()) {
            tempPeople.put(person.getPersonID(), person);
        }
        mapPeople.clear();
        for (Person person : allPeople.values()) {
            if (person.getGender().equals(g)) {
                if (tempPeople.containsKey(person.getPersonID())) {
                    mapPeople.put(person.getPersonID(), person);
                }
            }
        }
    }

    private void filterAncestors(String personID) {
        if (allPeople.get(personID).getMotherID() != null) {
            filterAncestors(allPeople.get(personID).getMotherID());
        }
        if (allPeople.get(personID).getFatherID() != null) {
            filterAncestors(allPeople.get(personID).getFatherID());
        }
        mapPeople.put(personID, allPeople.get(personID));
    }
}
