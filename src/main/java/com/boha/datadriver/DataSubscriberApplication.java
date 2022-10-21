package com.boha.datadriver;

import com.boha.datadriver.services.EventSubscriber;
import com.boha.datadriver.util.E;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.logging.Logger;
/**
 * App to subscribe to PubSub
 */
@SpringBootApplication
public class DataSubscriberApplication implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger LOGGER = Logger.getLogger(DataSubscriberApplication.class.getSimpleName());
	public static void main(String[] args) {

		LOGGER.info(E.BLUE_DOT+E.BLUE_DOT+ " DataSubscriberApplication starting .....");
		SpringApplication.run(DataSubscriberApplication.class, args);
		LOGGER.info(
				E.CHECK + E.CHECK +
				" DataSubscriberApplication completed starting. " + E.CHECK+E.CHECK);
	}
	@Value("${projectId}")
	private String projectId;
	@Value("${topicId}")
	private String topicId;
	@Autowired
	private EventSubscriber eventSubscriber;
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		LOGGER.info(E.RED_DOT+E.RED_DOT+ " Data Subscriber ApplicationReadyEvent - Timestamp: "
				+ E.YELLOW_STAR + event.getTimestamp());
		LOGGER.info(E.RED_DOT+E.RED_DOT+ " Project: " + projectId + " "
				+ E.YELLOW_STAR );
		LOGGER.info(E.RED_DOT+E.RED_DOT
				+  " Topic: " + topicId  + " " + E.YELLOW_STAR );
		try {
			eventSubscriber.subscribe();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


	}
}
