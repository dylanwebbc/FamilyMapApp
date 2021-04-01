package com.weebly.explearn.familymap.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import requests.RegisterRequest;
import results.LoginResult;

/**
 * An async task which registers a new user on the server
 */
public class RegisterAsyncTask {
    
    private static final String REGISTER_RESULT_KEY = "RegisterResultKey";

    /**
     * Submits the current task to a new single thread executor to keep the main thread clear
     *
     * @param handler a uiThreadMessageHandler which executes elsewhere when async task completes
     * @param hostName the hostname of the accessed server
     * @param portNumber the port number of the accessed server
     */
    public static void Execute(Handler handler, String hostName, String portNumber,
                               RegisterRequest registerRequest) {
        // Create and execute the task on a separate thread
        RegisterAsyncTask.RegisterTask registerTask = new RegisterAsyncTask.
                RegisterTask(handler, hostName, portNumber, registerRequest);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(registerTask);
    }

    private static class RegisterTask implements Runnable {

        private final String hostName;
        private final String portNumber;
        private final RegisterRequest registerRequest;

        private final Handler messageHandler;

        /**
         * Creates a new async register task
         *
         * @param messageHandler propagates a message containing the result of the registration
         * @param hostName the hostname of the accessed server
         * @param portNumber the portnumber of the accessed server
         */
        public RegisterTask(Handler messageHandler, String hostName, String portNumber,
                         RegisterRequest registerRequest) {
            this.messageHandler = messageHandler;
            this.hostName = hostName;
            this.portNumber = portNumber;
            this.registerRequest = registerRequest;
        }

        @Override
        public void run() {
            ServerProxy serverProxy = new ServerProxy(hostName, portNumber);
            LoginResult registerResult = serverProxy.register(registerRequest);
            sendMessage(registerResult.getAuthtoken(), registerResult.getPersonID(),
                    registerResult.getMessage());
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

            messageBundle.putStringArrayList(REGISTER_RESULT_KEY, result);
            message.setData(messageBundle);

            messageHandler.sendMessage(message);
        }
    }
}
