package com.example.healthtrackr.service;

import com.example.healthtrackr.entity.Exercise;
import com.example.healthtrackr.repository.ExerciseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class ExerciseService {
    @Value("${rapidapi.url}")
    private String rapidApiUrl;
    private final ExerciseRepository exerciseRepository;
    private static final String EXERCISE_DB_URL = "https://exercisedb.p.rapidapi.com/exercises";
    private static final String API_KEY = "d5c9ab47c8msh6e957f59f2e42dfp1e1f78jsn4f743099d751";
    private static final String API_HOST = "exercisedb.p.rapidapi.com";
    private final RestTemplate restTemplate;
    public List<Exercise> parseExercises(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseBody, new TypeReference<List<Exercise>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public ExerciseService(ExerciseRepository exerciseRepository, RestTemplate restTemplate) {
        this.exerciseRepository = exerciseRepository;

        this.restTemplate = restTemplate;
    }

    public List<Exercise> getAllExercises() {
        return fetchExercisesFromAPI();
    }



    private List<Exercise> fetchExercisesFromAPI() {
        String url = EXERCISE_DB_URL + "?limit=900&offset=0";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return parseExercises(response.getBody());
        } else {
            System.out.println("❌ Failed to fetch exercises. Status: " + response.getStatusCode());
            return new ArrayList<>();
        }
    }
    public void fetchAndSaveExercises() {
        List<Exercise> exercises = fetchExercisesFromAPI();
        // Sau này có thể lưu vào database
        System.out.println("Fetched " + exercises.size() + " exercises.");
    }
    public List<String> getBodyPartList() {
        String url = rapidApiUrl+ "/exercises/bodyPartList";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Host",API_HOST);
        headers.set("X-RapidAPI-Key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, String[].class);
            System.out.println("📩 Response Status: " + response.getStatusCode());
            System.out.println("📜 Response Body: " + Arrays.toString(response.getBody()));
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
            } else {
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            System.out.println("🚨 HTTP Error: " + e.getStatusCode());
            System.out.println("🚨 Response: " + e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }


    public List<Exercise> getExercisesByBodyPart(String bodyPart)  {
        String url = rapidApiUrl + "/exercises/bodyPart/" + bodyPart + "?limit=10&offset=0";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return parseExercises(response.getBody());
        } else {
            System.out.println("❌ Failed to fetch exercises by body part: " + bodyPart);
            return new ArrayList<>();
        }
    }

    public Page<Exercise> searchExercises(String name, String bodyPart, String equipment, String difficulty, Pageable pageable) {
        System.out.println("🔍 Searching exercises with: ");
        System.out.println("  📌 Name: " + name);
        System.out.println("  📌 BodyPart: " + bodyPart);
        System.out.println("  📌 Equipment: " + equipment);
        System.out.println("  📌 Difficulty: " + difficulty);
        System.out.println("  🔢 Page: " + pageable.getPageNumber() + " | Size: " + pageable.getPageSize());

        Specification<Exercise> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (bodyPart != null && !bodyPart.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("bodyPart"), bodyPart));
            }
            if (equipment != null && !equipment.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("equipment"), equipment));
            }
            if (difficulty != null && !difficulty.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return exerciseRepository.findAll(spec, pageable);
    }

}
