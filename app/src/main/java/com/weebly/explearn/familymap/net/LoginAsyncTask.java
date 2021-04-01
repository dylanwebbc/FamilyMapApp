package com.weebly.explearn.familymap.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.weebly.explearn.familymap.net.ServerProxy;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import requests.*;
import results.LoginResult;

/**
 * An async task which logs an existing user into the server
 */
public class LoginAsyncTask {

    private static final String LOGIN_RESULT_KEY = "LoginResultKey";

    /**
     * Submits the current task to a new single thread executor to keep the main thread clear
     *
     * @param handler a uiThreadMessageHandler which executes elsewhere when async task completes
     * @param hostName the hostname of the accessed server
     * @param portNumber the port number of the accessed server
     */
    public static void Execute(Handler handler, String hostName, String portNumber,
                               LoginRequest loginRequest) {
        // Create and execute the task on a separate thread
        LoginTask loginTask = new LoginTask(handler, hostName, portNumber,
                loginRequest);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(loginTask);
    }

    private static class LoginTask implements Runnable {

        private final String hostName;
        private final String portNumber;
        private final LoginRequest loginRequest;

        private final Handler messageHandler;

        /**
         * Creates a new async login task
         *
         * @param messageHandler propagates a message containing the result of the login
         * @param hostName the hostname of the accessed server
         * @param portNumber the portnumber of the accessed server
         */
        public LoginTask(Handler messageHandler, String hostName, String portNumber,
                         LoginRequest loginRequest) {
            this.messageHandler = messageHandler;
            this.hostName = hostName;
            this.portNumber = portNumber;
            this.loginRequest = loginRequest;
        }

        @Override
        public void run() {
            ServerProxy serverProxy = new ServerProxy(hostName, portNumber);
            LoginResult loginResult = serverProxy.login(loginRequest);
            sendMessage(loginResult.getAuthtoken(), loginResult.getPersonID(),
                    loginResult.getMessage());
        }

        /**
         *  Sends a message upon completion of the async task
         *
         * @param authtoken an authorization token used to verify the user's transaction
         * @param personID the ID of the user's corresponding person object in the family tree
         * @param resultMsg the result of the async task
         */
        private void sendMessage(String authtoken, String personID, String resultMsg) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();

            ArrayList<String> result = new ArrayList<>();
            result.add(authtoken);
            result.add(personID);
            result.add(resultMsg);

            messageBundle.putStringArrayList(LOGIN_RESULT_KEY, result);
            message.setData(messageBundle);

            messageHandler.sendMessage(message);
        }
    }
}
