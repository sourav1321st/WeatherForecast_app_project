package com.weather.forecast.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.weather.forecast.Service.WeatherService;
import com.weather.forecast.model.WeatherResponse;

@Controller
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    // Step 1: Show home page
    @GetMapping("/")
    public String showHomePage() {
        return "home"; // loads templates/home.html
    }

    // Step 2: When user enters a city name
    @GetMapping("/weather")
    public String getWeather(@RequestParam("city") String city, Model model) {
        WeatherResponse response = weatherService.getWeather(city);

        if (response == null || response.getList() == null || response.getList().isEmpty()) {
            model.addAttribute("error", "Invalid city name or no weather data found.");
            return "home";
        }

        model.addAttribute("city", city);
        model.addAttribute("weatherData", response);
        return "home";
    }

    // ✅ Step 3: When user allows location access (latitude and longitude)
    @GetMapping("/weather/location")
    public String getWeatherByLocation(@RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            Model model) {
        WeatherResponse response = weatherService.getWeatherByLocation(lat, lon);

        if (response == null || response.getList() == null || response.getList().isEmpty()) {
            model.addAttribute("error", "Unable to fetch weather data for your location.");
            return "home";
        }

        model.addAttribute("location", "Your Current Location");
        model.addAttribute("weatherData", response);
        return "home";
    }
    // ✅ Step 3b: JSON endpoint used by JavaScript for location-based weather

    @GetMapping("/api/weather/location")
    @ResponseBody
    public Map<String, Object> getWeatherJsonByLocation(@RequestParam("lat") double lat,
            @RequestParam("lon") double lon) {
        WeatherResponse weatherResponse = weatherService.getWeatherByLocation(lat, lon);
        String cityName = weatherService.getCityNameFromCoordinates(lat, lon);

        Map<String, Object> result = new HashMap<>();
        result.put("city", cityName);
        result.put("data", weatherResponse);
        return result;
    }
}