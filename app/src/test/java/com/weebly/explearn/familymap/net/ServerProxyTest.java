package com.weebly.explearn.familymap.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.weebly.explearn.familymap.model.DataCache;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import dbModels.User;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.EventsResult;
import results.LoginResult;
import results.PeopleResult;
import results.PersonResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServerProxyTest extends TestCase {

    private final String hostName = "localhost";
    private final String portNumber = "8080";
    private final ServerProxy serverProxy = new ServerProxy(hostName, portNumber);
    private LoginResult loginResult;
    private String testUsername;
    private String testPassword;

    @BeforeEach
    public void setUp() {
        testUsername = "dyl";
        testPassword = "1234";
        RegisterRequest registerRequest = new RegisterRequest(testUsername, testPassword,
                "dyl@gmail.com", "Dylan", "Webb", "m");
        loginResult = serverProxy.register(registerRequest);
    }

    @AfterEach
    public void tearDown() {
        serverProxy.clear();
        DataCache.getInstance().logout();
    }

    @Test
    public void testLoginPass() {
        LoginRequest loginRequest = new LoginRequest(testUsername, testPassword);
        LoginResult loginResult1 = serverProxy.login(loginRequest);
        assertNotSame(loginResult1.getAuthtoken(), loginResult.getAuthtoken());
        assertEquals(testUsername, loginResult1.getUsername());
        assertNull(loginResult1.getMessage());
    }

    @Test
    public void testLoginFail() {
        LoginRequest loginRequest = new LoginRequest(testUsername, "");
        LoginResult loginResult1 = serverProxy.login(loginRequest);
        assertFalse(loginResult1.isSuccess());

        LoginRequest loginRequest2 = new LoginRequest("", testPassword);
        LoginResult loginResult2 = serverProxy.login(loginRequest2);
        assertFalse(loginResult2.isSuccess());

        LoginRequest loginRequest3 = new LoginRequest(null, null);
        LoginResult loginResult3 = serverProxy.login(loginRequest3);
        assertFalse(loginResult3.isSuccess());

        LoginResult loginResult4 = serverProxy.login(null);
        assertFalse(loginResult4.isSuccess());
        assertEquals("Invalid input", loginResult4.getMessage());
    }

    @Test
    public void testRegisterPass() {
        // this is the result of the register from setUp
        assertTrue(loginResult.isSuccess());
        assertEquals(testUsername, loginResult.getUsername());
        assertNull(loginResult.getMessage());
        assertTrue(serverProxy.getAllEvents(loginResult.getAuthtoken()).isSuccess());
    }

    @Test
    public void testRegisterFail() {
        RegisterRequest registerRequest = new RegisterRequest("dad", "5678",
                "cooldad@gmail.com", "Father", "Webb", "m.");
        LoginResult loginResult1 = serverProxy.register(registerRequest);
        assertFalse(loginResult1.isSuccess());
        assertEquals("Invalid input", loginResult1.getMessage());

        LoginResult loginResult2 = serverProxy.register(null);
        assertFalse(loginResult1.isSuccess());
        assertEquals("Invalid input", loginResult2.getMessage());
    }

    @Test
    public void testGetAllPeoplePass() {
        PeopleResult peopleResult = serverProxy.getAllPeople(loginResult.getAuthtoken());
        assertTrue(peopleResult.isSuccess());
        assertEquals(31, peopleResult.getData().size());
        assertEquals(testUsername, peopleResult.getData().get(0).getAssociatedUsername());

        RegisterRequest registerRequest = new RegisterRequest("dad", "5678",
                "cooldad@gmail.com", "Father", "Webb", "m");
        LoginResult loginResult1 = serverProxy.register(registerRequest);
        PeopleResult peopleResult1 = serverProxy.getAllPeople(loginResult1.getAuthtoken());
        assertTrue(peopleResult1.isSuccess());
        assertEquals(31, peopleResult1.getData().size());
        assertEquals("dad", peopleResult1.getData().get(0).getAssociatedUsername());
    }

    @Test
    public void testGetAllPeopleFail() {
        PeopleResult peopleResult1 = serverProxy.getAllPeople("");
        assertEquals("Invalid input", peopleResult1.getMessage());

        PeopleResult peopleResult2 = serverProxy.getAllPeople(null);
        assertEquals("Invalid input", peopleResult2.getMessage());

        serverProxy.clear();
        PeopleResult peopleResult3 = serverProxy.getAllPeople(loginResult.getAuthtoken());
        assertFalse(peopleResult3.isSuccess());
    }

    @Test
    public void testGetAllEventsPass() {
        EventsResult eventsResult = serverProxy.getAllEvents(loginResult.getAuthtoken());
        assertTrue(eventsResult.isSuccess());
        assertEquals(91, eventsResult.getData().size());
        assertEquals(testUsername, eventsResult.getData().get(0).getAssociatedUsername());

        RegisterRequest registerRequest = new RegisterRequest("dad", "5678",
                "cooldad@gmail.com", "Father", "Webb", "m");
        LoginResult loginResult1 = serverProxy.register(registerRequest);
        EventsResult eventsResult1 = serverProxy.getAllEvents(loginResult1.getAuthtoken());
        assertTrue(eventsResult1.isSuccess());
        assertEquals(91, eventsResult1.getData().size());
        assertEquals("dad", eventsResult1.getData().get(0).getAssociatedUsername());
    }

    @Test
    public void testGetAllEventsFail() {
        EventsResult eventsResult1 = serverProxy.getAllEvents("");
        assertEquals("Invalid input", eventsResult1.getMessage());

        EventsResult eventsResult2 = serverProxy.getAllEvents(null);
        assertEquals("Invalid input", eventsResult2.getMessage());

        serverProxy.clear();
        EventsResult eventsResult3 = serverProxy.getAllEvents(loginResult.getAuthtoken());
        assertFalse(eventsResult3.isSuccess());
    }
}