package com.boha.datadriver.services;

import com.boha.datadriver.models.City;
import com.boha.datadriver.models.CityPlace;
import com.boha.datadriver.models.Event;
import com.boha.datadriver.util.E;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.firebase.cloud.FirestoreClient;
//import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class Generator {
    static final Logger LOGGER = Logger.getLogger(Generator.class.getSimpleName());

    public Generator(CityService cityService, PlacesService placesService) {
        this.cityService = cityService;
        this.placesService = placesService;
        LOGGER.info(E.LEAF+E.LEAF + " Generator constructed and services injected");
    }


//    private final PubSubAdmin pubSubAdmin;

    private  ArrayList<Subscriber> allSubscribers = new ArrayList<>();
    private final CityService cityService;
    private final PlacesService placesService;
    @Autowired
    private EventSubscriber eventSubscriber;

    Random random = new Random(System.currentTimeMillis());
    Timer timer;
    List<City> cityList;
    int totalCount = 0;
    int maxCount;
    public void generateEvents(long intervalInSeconds, int upperCountPerPlace, int maxCount) throws Exception {
        this.maxCount = maxCount;
        if (cityList == null || cityList.isEmpty()) {
            cityList = cityService.getCitiesFromFirestore();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    performWork(upperCountPerPlace);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, intervalInSeconds * 1000);

    }

    void stopTimer() {
        timer.cancel();
        timer = null;
        LOGGER.info(E.YELLOW_STAR+E.YELLOW_STAR+E.YELLOW_STAR+E.YELLOW_STAR+
                " Generator Timer stopped; events: " +E.LEAF+" "+ totalCount);
    }
    void performWork(int upperCountPerPlace) throws Exception {
        int index = random.nextInt(cityList.size() - 1);
        City city = cityList.get(index);
        List<CityPlace> places = placesService.getPlacesByCity(city.getId());

        int count = random.nextInt(upperCountPerPlace);
        if (count == 0) count = 5;

        for (int i = 0; i < count; i++) {
            int mIndex = random.nextInt(places.size() - 1);
            CityPlace cityPlace = places.get(mIndex);
            Event e = getEvent(cityPlace);
            writeEventToFirestore(e,city);
            sendToPubSub(e,city);
            totalCount++;
        }

        if (totalCount > maxCount) {
            stopTimer();
            totalCount = 0;
        }
    }

    private Event getEvent(CityPlace cityPlace) throws Exception {
        Event event = new Event();
        event.setCityPlace(cityPlace);
        int m = random.nextInt(300);
        if (m == 0) m = 150;
        event.setAmount(Double.parseDouble("" + m));
        int r = random.nextInt(5);
        if (r == 0) r = 1;
        event.setRating(r);
        event.setDate(new Date().toString());
        event.setLongDate(new Date().getTime());
        event.setEventId(UUID.randomUUID().toString());
        return event;
    }

    Firestore firestore = FirestoreClient.getFirestore();

    void writeEventToFirestore(Event event, City city) throws Exception{
        ApiFuture<DocumentReference> future = firestore.collection("events").add(event);
        LOGGER.info(E.LEAF+E.LEAF+ " Firestore event: "
                + E.ORANGE_HEART + event.getCityPlace().name + ", " + city.getCity()
                + " " + E.LEAF);
    }
    @Autowired
    private EventPublisher eventPublisher;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public void sendToPubSub(Event event, City city) throws Exception {
        eventPublisher.publish(GSON.toJson(event));
        LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+
                " PubSub Event: " + E.AMP + event.getCityPlace().name + ", " + city.getCity());
    }
}
