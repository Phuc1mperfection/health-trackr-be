package com.example.healthtrackr.service;

import com.example.healthtrackr.entity.Exercise;
import com.example.healthtrackr.exception.AppException;
import com.example.healthtrackr.repository.ExerciseRepository;
import com.example.healthtrackr.utils.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExerciseService {
    @Value("${rapidapi.url}")
    private String rapidApiUrl;
    private final ExerciseRepository exerciseRepository;
    private static final String EXERCISE_DB_URL = "https://exercisedb.p.rapidapi.com/exercises";
    private static final String API_KEY = "d5c9ab47c8msh6e957f59f2e42dfp1e1f78jsn4f743099d751";
    private static final String API_HOST = "exercisedb.p.rapidapi.com";
    private final RestTemplate restTemplate;

    // Cache để lưu trữ số lượng exercises cho mỗi bodyPart
    private Map<String, Integer> bodyPartCountCache = new HashMap<>();

    // Cache để lưu trữ số lượng exercises theo equipment và target
    private Map<String, Integer> equipmentCountCache = new HashMap<>();
    private Map<String, Integer> targetCountCache = new HashMap<>();

    public List<Exercise> parseExercises(String responseBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseBody, new TypeReference<List<Exercise>>() {
            });
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
        String url = rapidApiUrl + "/exercises/bodyPartList";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Host", API_HOST);
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

    /**
     * Lấy tổng số bài tập cho một bodyPart cụ thể
     * Sử dụng cache để tránh gọi API nhiều lần không cần thiết
     */
    private int getTotalExerciseCountForBodyPart(String bodyPart) {
        // Kiểm tra xem đã có trong cache chưa
        if (bodyPartCountCache.containsKey(bodyPart)) {
            return bodyPartCountCache.get(bodyPart);
        }

        // Nếu chưa có trong cache, gọi API để lấy tổng số bài tập
        String url = rapidApiUrl + "/exercises/bodyPart/" + bodyPart;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Gọi với limit lớn để lấy tổng số bài tập
            ResponseEntity<String> response = restTemplate.exchange(url + "?limit=1000", HttpMethod.GET, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> allExercises = parseExercises(response.getBody());
                int count = allExercises.size();

                // Lưu vào cache
                bodyPartCountCache.put(bodyPart, count);

                System.out.println("💡 Total exercises for body part '" + bodyPart + "': " + count);
                return count;
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching total count for body part '" + bodyPart + "': " + e.getMessage());
        }

        // Nếu có lỗi, trả về giá trị mặc định
        return 100;
    }

    public Page<Exercise> getExercisesByBodyPart(String bodyPart, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * pageable.getPageSize();

        String url = rapidApiUrl + "/exercises/bodyPart/" + bodyPart + "?limit=" + limit + "&offset=" + offset;

        System.out.println("🔍 Fetching exercises for body part: " + bodyPart);
        System.out.println("  🔢 Page: " + pageable.getPageNumber() + " | Size: " + pageable.getPageSize());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Trước tiên, lấy tổng số phần tử cho body part này (chỉ lấy 1 lần khi cần)
            int totalElements = getTotalExerciseCountForBodyPart(bodyPart);

            // Sau đó lấy dữ liệu cho trang hiện tại
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> exercises = parseExercises(response.getBody());
                // Kiểm tra nếu không có bài tập nào được tìm thấy
                if (exercises.isEmpty()) {
                    throw new AppException(ErrorCode.BODY_PART_NOT_FOUND);
                }

                return new org.springframework.data.domain.PageImpl<>(exercises, pageable, totalElements);
            } else {
                System.out.println("❌ Failed to fetch exercises by body part: " + bodyPart);
                throw new AppException(ErrorCode.BODY_PART_NOT_FOUND);
            }
        } catch (HttpClientErrorException.UnprocessableEntity e) {
            // Bắt lỗi 422 từ RapidAPI
            System.out.println("❌ Invalid body part: " + bodyPart);
            System.out.println("❌ Error: " + e.getResponseBodyAsString());

            // Tạo một thông báo lỗi tùy chỉnh từ phản hồi API
            final String initialMessage = "Invalid body part: " + bodyPart;

            // Trích xuất thông báo lỗi từ RapidAPI nếu có thể
            String extractedMessage = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(e.getResponseBodyAsString());
                String message = rootNode.path("message").asText();
                if (!message.isEmpty()) {
                    extractedMessage = message;
                }
            } catch (Exception ex) {
                // Nếu không thể parse JSON, sử dụng thông báo mặc định
                System.out.println("❌ Error parsing API response: " + ex.getMessage());
            }

            // Sử dụng thông báo trích xuất nếu có, nếu không sử dụng thông báo ban đầu
            final String finalMessage = extractedMessage != null ? extractedMessage : initialMessage;

            // Tạo một ngoại lệ tùy chỉnh với thông báo chi tiết
            throw new AppException(ErrorCode.BODY_PART_NOT_FOUND) {
                @Override
                public String getMessage() {
                    return finalMessage;
                }
            };
        } catch (Exception e) {
            // Bắt các lỗi khác
            System.out.println("❌ Error fetching exercises: " + e.getMessage());
            throw new AppException(ErrorCode.BODY_PART_NOT_FOUND);
        }
    }

    public Page<Exercise> searchExercises(String name, String bodyPart, String equipment, String difficulty,
            Pageable pageable) {
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

    public List<String> getEquipmentList() {
        String url = rapidApiUrl + "/exercises/equipmentList";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Host", API_HOST);
        headers.set("X-RapidAPI-Key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, String[].class);
            System.out.println("📩 Equipment list - Response Status: " + response.getStatusCode());
            System.out.println("📜 Equipment list - Response Body: " + Arrays.toString(response.getBody()));

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
            } else {
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            System.out.println("🚨 Equipment list - HTTP Error: " + e.getStatusCode());
            System.out.println("🚨 Equipment list - Response: " + e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<String> getTargetList() {
        String url = rapidApiUrl + "/exercises/targetList";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Host", API_HOST);
        headers.set("X-RapidAPI-Key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, String[].class);
            System.out.println("📩 Target list - Response Status: " + response.getStatusCode());
            System.out.println("📜 Target list - Response Body: " + Arrays.toString(response.getBody()));

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
            } else {
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            System.out.println("🚨 Target list - HTTP Error: " + e.getStatusCode());
            System.out.println("🚨 Target list - Response: " + e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Page<Exercise> getExercisesByEquipment(String equipment, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * pageable.getPageSize();

        String url = rapidApiUrl + "/exercises/equipment/" + equipment + "?limit=" + limit + "&offset=" + offset;

        System.out.println("🔍 Fetching exercises for equipment: " + equipment);
        System.out.println("  🔢 Page: " + pageable.getPageNumber() + " | Size: " + pageable.getPageSize());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Trước tiên, lấy tổng số phần tử cho equipment này
            int totalElements = getTotalExerciseCountForEquipment(equipment);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> exercises = parseExercises(response.getBody());
                if (exercises.isEmpty()) {
                    throw new AppException(ErrorCode.EQUIPMENT_NOT_FOUND);
                }

                return new org.springframework.data.domain.PageImpl<>(exercises, pageable, totalElements);
            } else {
                System.out.println("❌ Failed to fetch exercises by equipment: " + equipment);
                throw new AppException(ErrorCode.EQUIPMENT_NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching exercises by equipment: " + e.getMessage());
            throw new AppException(ErrorCode.EQUIPMENT_NOT_FOUND);
        }
    }

    public Page<Exercise> getExercisesByTarget(String target, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * pageable.getPageSize();

        String url = rapidApiUrl + "/exercises/target/" + target + "?limit=" + limit + "&offset=" + offset;

        System.out.println("🔍 Fetching exercises for target: " + target);
        System.out.println("  🔢 Page: " + pageable.getPageNumber() + " | Size: " + pageable.getPageSize());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Trước tiên, lấy tổng số phần tử cho target này
            int totalElements = getTotalExerciseCountForTarget(target);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> exercises = parseExercises(response.getBody());
                if (exercises.isEmpty()) {
                    throw new AppException(ErrorCode.TARGET_NOT_FOUND);
                }

                return new org.springframework.data.domain.PageImpl<>(exercises, pageable, totalElements);
            } else {
                System.out.println("❌ Failed to fetch exercises by target: " + target);
                throw new AppException(ErrorCode.TARGET_NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching exercises by target: " + e.getMessage());
            throw new AppException(ErrorCode.TARGET_NOT_FOUND);
        }
    }

    private int getTotalExerciseCountForEquipment(String equipment) {
        if (equipmentCountCache.containsKey(equipment)) {
            return equipmentCountCache.get(equipment);
        }

        String url = rapidApiUrl + "/exercises/equipment/" + equipment;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url + "?limit=1000", HttpMethod.GET, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> allExercises = parseExercises(response.getBody());
                int count = allExercises.size();

                equipmentCountCache.put(equipment, count);

                System.out.println("💡 Total exercises for equipment '" + equipment + "': " + count);
                return count;
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching total count for equipment '" + equipment + "': " + e.getMessage());
        }

        return 100; // Default if error
    }

    private int getTotalExerciseCountForTarget(String target) {
        if (targetCountCache.containsKey(target)) {
            return targetCountCache.get(target);
        }

        String url = rapidApiUrl + "/exercises/target/" + target;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url + "?limit=1000", HttpMethod.GET, entity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> allExercises = parseExercises(response.getBody());
                int count = allExercises.size();

                targetCountCache.put(target, count);

                System.out.println("💡 Total exercises for target '" + target + "': " + count);
                return count;
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching total count for target '" + target + "': " + e.getMessage());
        }

        return 100; // Default if error
    }

    /**
     * Search exercises by name using RapidAPI endpoint with pagination
     */
    public Page<Exercise> searchExercisesByName(String name, Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        String url = rapidApiUrl + "/exercises/name/" + name + "?limit=" + limit + "&offset=" + offset;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                List<Exercise> exercises = parseExercises(response.getBody());
                // Không có total count từ API, trả về size hiện tại
                return new org.springframework.data.domain.PageImpl<>(exercises, pageable, exercises.size());
            } else {
                return Page.empty(pageable);
            }
        } catch (Exception e) {
            System.out.println("❌ Error searching exercises by name: " + e.getMessage());
            return Page.empty(pageable);
        }
    }
}
