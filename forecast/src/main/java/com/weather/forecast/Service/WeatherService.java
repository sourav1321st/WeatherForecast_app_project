package com.weather.forecast.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.model.Main;
import com.weather.forecast.model.Weather;
import com.weather.forecast.model.WeatherData;
import com.weather.forecast.model.WeatherResponse;
import com.weather.forecast.model.Wind;

@Service
public class WeatherService {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // 🌤️ Get weather by city name (existing)
    public WeatherResponse getWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric";
        String response = restTemplate.getForObject(url, String.class);

        List<WeatherData> weatherDataList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);

            // ✅ Handle invalid city name
            if (root.has("cod") && !"200".equals(root.get("cod").asText())) {
                System.err.println("API Error: " + root.get("message").asText());
                return null;
            }

            JsonNode list = root.get("list");

            for (JsonNode node : list) {
                String dateTime = node.get("dt_txt").asText();

                JsonNode mainNode = node.get("main");
                double temp = mainNode.get("temp").asDouble();
                int humidity = mainNode.get("humidity").asInt();

                double windSpeed = node.get("wind").get("speed").asDouble();

                JsonNode weatherArray = node.get("weather").get(0);
                String mainCondition = weatherArray.get("main").asText();
                String description = weatherArray.get("description").asText();

                Weather weather = new Weather();
                weather.setMain(mainCondition);
                weather.setDescription(description);

                Main main = new Main();
                main.setTemp(temp);
                main.setHumidity(humidity);

                Wind wind = new Wind();
                wind.setSpeed(windSpeed);

                WeatherData weatherData = new WeatherData();
                weatherData.setDt_txt(dateTime);
                weatherData.setMain(main);
                weatherData.setWind(wind);
                weatherData.setWeather(Collections.singletonList(weather));

                weatherDataList.add(weatherData);
            }

        } catch (JsonProcessingException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
        }

        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setList(weatherDataList);
        return weatherResponse;
    }

    // 📍 NEW: Get weather by geographic coordinates
    public WeatherResponse getWeatherByLocation(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.has("cod") && root.get("cod").asInt() != 200) {
                System.err.println("Location-based API Error: " + root.get("message").asText());
                return null;
            }

            double temp = root.get("main").get("temp").asDouble();
            int humidity = root.get("main").get("humidity").asInt();
            double windSpeed = root.get("wind").get("speed").asDouble();

            String mainCondition = root.get("weather").get(0).get("main").asText();
            String description = root.get("weather").get(0).get("description").asText();

            Weather weather = new Weather();
            weather.setMain(mainCondition);
            weather.setDescription(description);

            Main main = new Main();
            main.setTemp(temp);
            main.setHumidity(humidity);

            Wind wind = new Wind();
            wind.setSpeed(windSpeed);

            WeatherData weatherData = new WeatherData();
            weatherData.setMain(main);
            weatherData.setWind(wind);
            weatherData.setWeather(Collections.singletonList(weather));
            weatherData.setDt_txt("Current");

            WeatherResponse weatherResponse = new WeatherResponse();
            weatherResponse.setList(Collections.singletonList(weatherData));

            return weatherResponse;

        } catch (JsonProcessingException e) {
            System.err.println("Error parsing location-based response: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error (location): " + e.getMessage());
        }

        return null;
    }

    public String getCityNameFromCoordinates(double lat, double lon) {
        String url = "http://api.openweathermap.org/geo/1.0/reverse?lat=" + lat
                + "&lon=" + lon + "&limit=1&appid=" + apiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            if (root.isArray() && root.size() > 0) {
                return root.get(0).get("name").asText(); // city name
            }
        } catch (JsonProcessingException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
        } catch (RestClientException e) {
            System.err.println("HTTP request error: " + e.getMessage());
        }
        return "Unknown Location";
    }

}