package com.scheme.chc.lockscreen.weather;


import java.io.Serializable;

public class locationModel implements Serializable {

    private float longitude;
    private float latitude;
    private int id;
    private long sunset;
    private long sunrise;
    private String country;
    private String city;

    public int getid() {return id;};
    void setid(int id) {this.id = id;};
    public float getLongitude() {
        return longitude;
    }
    void setLongitude(float longitude) {
        this.longitude = longitude;
    }
    public float getLatitude() {
        return latitude;
    }
     void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    public long getSunset() {
        return sunset;
    }
     void setSunset(long sunset) {
        this.sunset = sunset;
    }
    public long getSunrise() {
        return sunrise;
    }
     void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }
    public String getCountry() {
        return country;
    }
     void setCountry(String country) {
        this.country = country;
    }
    public String getCity() {
        return city;
    }
     void setCity(String city) {
        this.city = city;
    }




}