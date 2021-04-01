package com.weebly.explearn.familymap.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.weebly.explearn.familymap.model.DataCache;
import com.weebly.explearn.familymap.net.ServerProxy;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import results.EventsResult;
import results.PeopleResult;
import dbModels.*;

/**
 * An async task which retrieves data from the server for the current user's family tree
 */
public class DataAsyncTask {

    private static final String DATA_RETRIEVAL_KEY = "DataRetrievalKey";

    /**
     * Submits the current task to a new single thread executor to keep the main thread clear
     *
     * @param handler a uiThreadMessageHandler which executes elsewhere when async task completes
     * @param hostName the hostname of the accessed server
     * @param portNumber the port number of the accessed server
     * @param authtoken an authorization token used to verify the user's transaction
     */
    public static void Execute(Handler handler, String hostName, String portNumber,
                               String authtoken) {
        // Create and execute the task on a separate thread
        DataTask dataTask = new DataTask(handler, hostName, portNumber, authtoken);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(dataTask);
    }

    private static class DataTask implements Runnable {

        private final String hostName;
        private final String portNumber;
        private final String authtoken;

        private final Handler messageHandler;

        /**
         * Creates a new async data task
         *
         * @param messageHandler propagates a message containing the result of the data retrieval
         * @param hostName the hostname of the accessed server
         * @param portNumber the portnumber of the accessed server
         * @param authtoken an authorization token used to verify the user's transaction
         */
        public DataTask(Handler messageHandler, String hostName, String portNumber,
                        String authtoken) {
            this.messageHandler = messageHandler;
            this.hostName = hostName;
            this.portNumber = portNumber;
            this.authtoken = authtoken;
        }

        @Override
        public void run() {
            ServerProxy serverProxy = new ServerProxy(hostName, portNumber);
            PeopleResult peopleResult = serverProxy.getAllPeople(authtoken);
            EventsResult eventsResult = serverProxy.getAllEvents(authtoken);

            if (!peopleResult.isSuccess()) {
                sendMessage(peopleResult.getMessage());
            }
            else if (!eventsResult.isSuccess()) {
                sendMessage(eventsResult.getMessage());
            }
            else {
                ArrayList<Person> people = peopleResult.getData();
                ArrayList<Event> events = eventsResult.getData();
                DataCache.getInstance().insertPeople(people);
                DataCache.getInstance().insertEvents(events);
                sendMessage(null);
            }
        }

        /**
         * Sends a message upon completion of the async task
         *
         * @param resultMsg the result of the async task
         */
        private void sendMessage(String resultMsg) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();

            messageBundle.putString(DATA_RETRIEVAL_KEY, resultMsg);
            message.setData(messageBundle);

            messageHandler.sendMessage(message);
        }
    }
}
