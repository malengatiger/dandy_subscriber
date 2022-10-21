package com.boha.datadriver.services;

import com.boha.datadriver.models.Event;
import com.boha.datadriver.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
/**
 *
 */
@Service
public class EventSubscriber {
    static final Logger LOGGER = Logger.getLogger(EventSubscriber.class.getSimpleName());
    @Value("${projectId}")
    private String projectId;
    @Value("${subscriptionId}")
    private String subscriptionId;

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public void subscribe() throws Exception {
        Subscriber subscriber;
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, subscriptionId);
        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    Event e = GSON.fromJson(message.getData().toStringUtf8(),Event.class);
                    LOGGER.info(E.AMP+E.AMP+" Received: " + E.PEAR + " Rating: " + e.getRating() +
                            E.PEAR + " " + e.getCityPlace().name + ", "
                            + e.getCityPlace().cityName);

                    consumer.ack();
                };


//        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();
            LOGGER.info(E.PEAR+E.PEAR+" Listening to messages on " + subscriptionName);
            // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
            //subscriber.awaitTerminated(30, TimeUnit.SECONDS);
//        } catch (TimeoutException timeoutException) {
//            LOGGER.info("Subscription has timed out! " + E.RED_APPLE+E.RED_APPLE);
//
//        }
    }

}
