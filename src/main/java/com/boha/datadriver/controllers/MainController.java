package com.boha.datadriver.controllers;

import com.boha.datadriver.models.City;
import com.boha.datadriver.models.CityPlace;
import com.boha.datadriver.services.CityService;
import com.boha.datadriver.services.Generator;
import com.boha.datadriver.services.PlacesService;
import com.boha.datadriver.util.E;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
@RestController
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getSimpleName());

    public MainController(CityService cityService) {
        this.cityService = cityService;
    }


    private final CityService cityService;

    @GetMapping("/saveCities")
    private ResponseEntity<Object> saveCities() {
        try {
            List<City> cities = cityService.addCitiesToFirestore();
            LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+E.CHECK+
                    " MainController returning " + cities.size() + " cities");
            return ResponseEntity.ok(cities);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }
    @GetMapping("/getCities")
    private ResponseEntity<Object> getCities() {
        try {
            List<City> cities = cityService.getCitiesFromFirestore();
            LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+E.CHECK+
                    " Firestore Returning " + cities.size() + " cities " + E.CHECK);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }
    @Autowired
    PlacesService placesService;
    @GetMapping("/loadCityPlaces")
    private ResponseEntity<Object> loadCityPlaces() {
        try {
            String loaded = placesService.loadCityPlaces();
            LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+E.CHECK+
                    "  " + loaded + E.CHECK);
            return ResponseEntity.ok(loaded);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }
    @GetMapping("/getPlacesByCity")
    private ResponseEntity<Object> getPlacesByCity(@RequestParam String cityId) {
        try {
            List<CityPlace> placesByCity = placesService.getPlacesByCity(cityId);
            LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+E.CHECK+
                    "  City Places Found: " + placesByCity.size() + " " + E.CHECK);
            return ResponseEntity.ok(placesByCity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }

    @Autowired
    Generator generator;
    @GetMapping("/generateEvents")
    private ResponseEntity<Object> generateEvents(long intervalInSeconds, int upperCountPerPlace, int maxCount)  {
        try {
            LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+
                            "  Generator starting .... ");
                    generator.generateEvents(intervalInSeconds,upperCountPerPlace,maxCount);
//            LOGGER.info(E.BLUE_HEART+E.BLUE_HEART+E.CHECK+
//                    "  Generator completed: " + " " + E.CHECK);
            return ResponseEntity.ok("Generator has started. Check logs and Firestore");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }
    }


}
