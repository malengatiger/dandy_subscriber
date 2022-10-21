package com.boha.datadriver.services;

import com.boha.datadriver.models.City;
import com.boha.datadriver.util.E;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service
public class CityService {
    private static final Logger LOGGER = Logger.getLogger(CityService.class.getSimpleName());

    public CityService() {
        LOGGER.info(E.AMP+E.AMP+E.AMP + " CityService constructed");
        initFirebase();
    }

    public List<City>  getCitiesFromFile() throws IOException{

        File file = ResourceUtils.getFile("classpath:za.json");
        LOGGER.info(E.ORANGE_HEART+E.ORANGE_HEART+
                " Cities File, length: " + file.length());


        String json = Files.readString(file.toPath());
//        LOGGER.info(json);
        Gson gson = new Gson();
        City[] cities = gson.fromJson(json, City[].class);
        LOGGER.info(E.BLUE_DOT+E.BLUE_DOT+ " Found " + cities.length + " cities from file");
        int ind= 0;
        List<City> realCities = new ArrayList<>();
        realCities.addAll(Arrays.asList(cities));
        for (City city : realCities) {
            city.setLatitude(Double.parseDouble(city.getLat()));
            city.setLongitude(Double.parseDouble(city.getLng()));
            city.setId(UUID.randomUUID().toString());
        }

        LOGGER.info(E.BLUE_DOT+E.BLUE_DOT+ " Found " + realCities.size() + " real cities from file");

        return realCities;
    }
    public List<City> addCitiesToFirestore() throws IOException{
        List<City> cities = getCitiesFromFile();
        Firestore c = FirestoreClient.getFirestore();

        for (City realCity : cities) {
            LOGGER.info(E.RED_APPLE + " " + realCity.getCity()
                    + " to Firestore " + E.RED_APPLE);
            ApiFuture<DocumentReference> future = c.collection("cities").add(realCity);
            try {
                LOGGER.info(E.GREEN_APPLE+" Firestore city; path: " + future.get().getPath());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return cities;
    }
    public List<City> getCitiesFromFirestore() throws Exception {
        Firestore c = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = c.collection("cities")
                .orderBy("city")
                .get();
        QuerySnapshot snapshot = future.get();
        List<QueryDocumentSnapshot> docs = snapshot.getDocuments();
        List<City> resultCities = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
           City city = doc.toObject(City.class);
           resultCities.add(city);
        }
        if (resultCities.isEmpty()) {
            resultCities = addCitiesToFirestore();
        }
        LOGGER.info(E.CHECK + E.CHECK + " Found " + resultCities.size()  + " cities from Firestore");
        return resultCities;
    }
    public City getCityById(String cityId) throws Exception {
        Firestore firestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = firestore.collection("cities")
                .whereEqualTo("id", cityId)
                .get();
        QuerySnapshot snapshot = future.get();
        List<QueryDocumentSnapshot> docs = snapshot.getDocuments();
        City city = null;
        for (QueryDocumentSnapshot doc : docs) {
            city = doc.toObject(City.class);
        }
        if (city == null) {
            LOGGER.info("City not found!");
        } else {
            LOGGER.info(E.CHECK + E.CHECK + " Found " + city.getCity() + " from Firestore");
        }
        return city;
    }

    void initFirebase() {
        FirebaseOptions options = null;
        try {
            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setDatabaseUrl("https://thermal-effort-366015.firebaseio.com/")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FirebaseApp.initializeApp(options);
        LOGGER.info(E.GREEN_APPLE+E.GREEN_APPLE+E.GREEN_APPLE+
                "Firebase has been initialized");

    }
}
