package com.weather.forecast.model;

import java.util.List;

public class WeatherResponse {
    private City city;
    private List<WeatherData> list;

    // Getters and Setters
    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public List<WeatherData> getList() {
        return list;
    }

    public void setList(List<WeatherData> list) {
        this.list = list;
    }
}