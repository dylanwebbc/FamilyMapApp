package com.weebly.explearn.familymap.net;

import android.util.EventLog;
import android.util.Log;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;

import results.*;
import requests.*;

/**
 * An interface between the async tasks and the server
 */
public class ServerProxy {

    public static String hostName;
    public static String portNumber;

    /**
     * creates a new server proxy
     *
     * @param hostName the hostname of the accessed server
     * @param portNumber the portnumber of the accessed server
     */
    public ServerProxy(String hostName, String portNumber) {
        setHostName(hostName);
        setPortNumber(portNumber);
    }

    /**
     * Handles the /user/login command
     *
     * @param loginRequest an object containing the request data for the operation
     * @return an object containing the result data for the operation
     */
    public LoginResult login(LoginRequest loginRequest) {
        try {
            String requestData = serialize(loginRequest);
            String responseData = processHttp(requestData, "/user/login");
            if (responseData.charAt(0) == '{') {
                return deserialize(responseData, LoginResult.class);
            }
            else {
                return new LoginResult(responseData);
            }
        }
        catch (IOException exception) {
            return new LoginResult("Json error");
        }
    }

    /**
     * Handles the /user/register command
     *
     * @param registerRequest an object containing the request data for the operation
     * @return an object containing the result data for the operation
     */
    public LoginResult register(RegisterRequest registerRequest) {
        try {
            String requestData = serialize(registerRequest);
            String responseData = processHttp(requestData, "/user/register");
            if (responseData.charAt(0) == '{') {
                return deserialize(responseData, LoginResult.class);
            }
            else {
                return new LoginResult(responseData);
            }
        }
        catch (IOException exception) {
            return new LoginResult("Json error");
        }
    }

    /**
     * Handles the /person command
     *
     * @param authtoken an authorization token used to verify the user's transaction
     * @return an object containing the result data for the operation (all related person objects)
     */
    public PeopleResult getAllPeople(String authtoken) {
        try {
            String responseData = processHttp(authtoken, "/person");
            if (responseData.charAt(0) == '{') {
                return deserialize(responseData, PeopleResult.class);
            }
            else {
                return new PeopleResult(responseData);
            }
        }
        catch (IOException exception) {
            return new PeopleResult("Json error");
        }
    }

    /**
     * Handles the /event command
     *
     * @param authtoken an authorization token used to verify the user's transaction
     * @return an object containing the result data for the operation (all related event objects)
     */
    public EventsResult getAllEvents(String authtoken) {
        try {
            String responseData = processHttp(authtoken, "/event");
            if (responseData.charAt(0) == '{') {
                return deserialize(responseData, EventsResult.class);
            }
            else {
                return new EventsResult(responseData);
            }
        }
        catch (IOException exception) {
            return new EventsResult("Json error");
        }
    }

    /**
     * Handles the /clear command
     *
     * @return an object containing the result data for the operation
     */
    public Result clear() {
        try {
            String responseData = processHttp("", "/clear");
            if (responseData.charAt(0) == '{') {
                return deserialize(responseData, Result.class);
            }
            else {
                return new Result(true, responseData);
            }
        }
        catch (IOException exception) {
            return new Result(false, "Json error");
        }
    }

    /**
     * Opens an http connection to the server and processes the specified http request by
     * sending request data and retrieving result data
     *
     * @param requestData a string containing the request data for the operation
     * @param httpCommand a string containing the http command to be executed
     * @return the result data as a string
     */
    private String processHttp(String requestData, String httpCommand) {
        try {
            URL url = new URL("http://" + hostName + ":" + portNumber + httpCommand);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.addRequestProperty("Content-Type", "application/json");

            if (httpCommand.equals("/person") || httpCommand.equals("/event")) {
                http.setRequestMethod("GET");
                http.setDoOutput(false);
                http.addRequestProperty("Authorization", requestData);
            }
            else {
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                OutputStream requestBody = http.getOutputStream();
                writeString(requestData, requestBody);
                requestBody.close();
            }

            http.connect();

            InputStream responseBody = http.getInputStream();
            String responseData = readString(responseBody);
            return responseData;
        }
        catch (MalformedURLException exception) {
            return "Invalid URL";
        }
        catch (IOException exception) {
            return "Invalid input";
        }
    }

    /**
     * Converts an Object to Json
     *
     * @param object the object to be converted
     * @return a Json representation of the Object
     * @throws IOException if an input/output error occurs
     */
    private static String serialize(Object object) throws IOException {
        return (new Gson()).toJson(object);
    }

    /**
     * Converts Json to an Object
     *
     * @param json the Json to be converted
     * @param returnType the resulting Object type
     * @return an Object representation of the Json
     * @throws IOException if an input/output error occurs
     */
    private static <T> T deserialize(String json, Class<T> returnType) throws IOException {
        return (new Gson()).fromJson(json, returnType);
    }

    /**
     * Reads an InputStream and converts it to a StringBuilder
     *
     * @param is an InputStream
     * @return a StringBuilder representation of the InputStream
     * @throws IOException if an input/output error occurs
     */
    private String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    /**
     * Writes a string to an OutputStream
     *
     * @param str the string to be written
     * @param os the OutputStream to be written to
     * @throws IOException if an input/output error occurs
     */
    private void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

    public static String getHostName() {
        return hostName;
    }

    public static void setHostName(String hostName) {
        ServerProxy.hostName = hostName;
    }

    public static String getPortNumber() {
        return portNumber;
    }

    public static void setPortNumber(String portNumber) {
        ServerProxy.portNumber = portNumber;
    }
}
