package com.example.healthtrackr.service;

import com.example.healthtrackr.entity.Exercise;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ExerciseApiService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_HOST = "exercisedb.p.rapidapi.com";
    @Value("${rapidapi.exercise.url}")
    private String rapidApiUrl;
    @Value("${rapidapi.key}")
    private String rapidApiKey;
    private static final String API_KEY = "d5c9ab47c8msh6e957f59f2e42dfp1e1f78jsn4f743099d751";

    public List<Exercise> getExercisesFromApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", API_KEY);
        headers.set("X-RapidAPI-Host", API_HOST);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Exercise[]> response = restTemplate.exchange(
                    rapidApiUrl  , HttpMethod.GET, entity, Exercise[].class
            );

            return Arrays.asList(Objects.requireNonNull(response.getBody()));
        } catch (HttpClientErrorException e) {
            // Xử lý lỗi 403
            System.out.println("Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Collections.emptyList(); // Hoặc xử lý theo cách khác
        }
    }
}
