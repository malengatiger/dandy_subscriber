package com.boha.datadriver.models;

public class Event {
    private String eventId;
    private String date;
    private CityPlace cityPlace;
    private double amount;
    private int rating;
    private long longDate;

    public long getLongDate() {
        return longDate;
    }

    public void setLongDate(long longDate) {
        this.longDate = longDate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CityPlace getCityPlace() {
        return cityPlace;
    }

    public void setCityPlace(CityPlace cityPlace) {
        this.cityPlace = cityPlace;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
