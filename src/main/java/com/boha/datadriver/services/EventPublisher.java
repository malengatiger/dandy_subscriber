package com.boha.datadriver.services;

import com.boha.datadriver.util.E;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
/**
 *
 */
@Service
public class EventPublisher {
    static final Logger LOGGER = Logger.getLogger(EventPublisher.class.getSimpleName());

    @Value("${projectId}")
    private String projectId;
    @Value("${topicId}")
    private String topicId;
    Publisher publisher;

    public void publish(String message)
            throws Exception {
        TopicName topicName = TopicName.of(projectId, topicId);


        if (publisher == null) {
            publisher = Publisher.newBuilder(topicName.getTopic()).build();
            LOGGER.info(E.RED_DOT + E.RED_DOT +
                    " PubSub Publisher has been created: " + publisher.toString()
                    + " " + E.GREEN_APPLE);
            LOGGER.info(E.RED_DOT + E.RED_DOT +
                    " Topic: " + topicName.getTopic()
                    + " " + E.GREEN_APPLE);

        }
        ByteString data = ByteString.copyFromUtf8(message);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
        // Once published, returns a server-assigned message id (unique within the topic)
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        LOGGER.info("PubSub message published " + E.AMP + E.AMP + E.AMP + " id: " + messageIdFuture.get());

    }

    void closePublisher() throws InterruptedException {
        if (publisher != null) {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}
