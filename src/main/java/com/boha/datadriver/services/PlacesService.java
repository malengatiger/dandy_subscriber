package com.boha.datadriver.services;

import com.boha.datadriver.models.City;
import com.boha.datadriver.models.CityPlace;
import com.boha.datadriver.models.Root;
import com.boha.datadriver.util.E;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
/**
 *
 */
@Service
public class PlacesService {
    private static final String prefix =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final Logger LOGGER = Logger.getLogger(PlacesService.class.getSimpleName());

    String buildLink(double lat, double lng, int radiusInMetres) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("location=").append(lat).append(",").append(lng);
        sb.append("&radius=").append(radiusInMetres);
        sb.append("&key=AIzaSyCDiIBwKgGf2z9rseOFn8GjkrJluHpCDl4");
        return sb.toString();
    }
    OkHttpClient client = new OkHttpClient();
    int MAX_PAGE_COUNT = 2;
    int pageCount;
    int totalPlaceCount = 0;
    void getCityPlaces(City city, int radiusInMetres, String pageToken) throws Exception {
        if (pageToken == null) {
            if (city.getCity().contains("Durban")
                    || city.getCity().contains("Pretoria")
                    || city.getCity().contains("Cape Town")
                    || city.getCity().contains("Johannesburg" )
                    || city.getCity().contains("Sandton" )
                    || city.getCity().contains("Bloemfontein")
                    || city.getCity().contains("Fourways" )) {
                MAX_PAGE_COUNT = 4;
                LOGGER.info(E.RED_DOT+E.RED_DOT+E.RED_DOT +
                        " MAX_PAGE_COUNT = 4 !!! Yay! " + city.getCity());
            } else {
                MAX_PAGE_COUNT =  1;
            }
        }
        String link = buildLink(city.getLatitude(),city.getLongitude(),radiusInMetres);
        if (pageToken != null) {
            link += "&pagetoken="+pageToken;
        }
        LOGGER.info(E.YELLOW_STAR+E.YELLOW_STAR+ " " + link);
        HttpUrl.Builder urlBuilder
                = HttpUrl.parse(link).newBuilder();

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String mResp = response.body().string();
        Root root = GSON.fromJson(mResp, Root.class);
        for (CityPlace cityPlace : root.results) {
            cityPlace.cityId = city.getId();
        }
        addCityPlacesToFirestore(root);
        pageCount++;
        totalPlaceCount += root.results.size();
        if (pageCount < MAX_PAGE_COUNT) {
            if (root.next_page_token != null) {
                getCityPlaces(city, radiusInMetres, root.next_page_token);
            }
        }
    }

    void addCityPlacesToFirestore(Root root) throws Exception {
        Firestore c = FirestoreClient.getFirestore();
        for (CityPlace cityPlace : root.results) {
            ApiFuture<DocumentReference> future = c.collection("cityPlaces").add(cityPlace);
            DocumentReference ref = future.get();
            LOGGER.info(E.RED_APPLE+E.RED_APPLE+
                    " " + cityPlace.name + E.YELLOW_STAR + " path: " + ref.getPath());
        }
    }
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Autowired
    private CityService cityService;

    public List<CityPlace> getPlacesByCity(String cityId) throws Exception {
        List<CityPlace> cityPlaces = new ArrayList<>();
        Firestore c = FirestoreClient.getFirestore();
        City city = cityService.getCityById(cityId);
        ApiFuture<QuerySnapshot> future = c.collection("cityPlaces")
                .whereEqualTo("cityId", cityId)
                .get();
        QuerySnapshot snapshot = future.get();
        List<QueryDocumentSnapshot> list = snapshot.getDocuments();

        for (QueryDocumentSnapshot queryDocumentSnapshot : list) {
            CityPlace cityPlace = queryDocumentSnapshot.toObject(CityPlace.class);
            cityPlaces.add(cityPlace);
        }

        LOGGER.info(E.RED_DOT + E.RED_DOT + E.RED_DOT + E.RED_DOT +
                " " + city.getCity() + " has " + cityPlaces.size() + " places on file" );
        return cityPlaces;
    }
    public String loadCityPlaces() throws Exception {
        List<City> cities = cityService.getCitiesFromFirestore();
        if (!cities.isEmpty()) {
            for (City city : cities) {
                LOGGER.info(E.BLUE_DOT + E.BLUE_DOT + E.BLUE_DOT +
                        " Finding places for " + city.getCity());
                pageCount = 0;
                getCityPlaces(city, 10000, null);

            }
        }

        return totalPlaceCount + " Total City Places Loaded " + E.AMP+E.AMP+E.AMP;
    }
}
