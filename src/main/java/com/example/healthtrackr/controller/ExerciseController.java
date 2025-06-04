package com.example.healthtrackr.controller;

import com.example.healthtrackr.entity.Exercise;
import com.example.healthtrackr.service.ExerciseApiService;
import com.example.healthtrackr.service.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final ExerciseApiService exerciseApiService;

    public ExerciseController(ExerciseService exerciseService, ExerciseApiService exerciseApiService) {
        this.exerciseService = exerciseService;
        this.exerciseApiService = exerciseApiService;
    }

    @GetMapping
    public List<Exercise> getAllExercises() {
        return exerciseService.getAllExercises();
    }

    /**
     * Endpoint for exercises filtered by body part with pagination
     * Throws AppException with BODY_PART_NOT_FOUND error code if no exercises found
     * Returns a PageResponse with content and pagination metadata
     */
    @GetMapping("/search-by-bodypart")
    public Map<String, Object> getExercisesByBodyPartParam(
            @RequestParam(name = "bodyPart") String bodyPart,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Phương thức này sẽ ném AppException nếu không tìm thấy bài tập cho bodyPart
        Page<Exercise> exercisePage = exerciseService.getExercisesByBodyPart(bodyPart, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", exercisePage.getContent());
        response.put("totalElements", exercisePage.getTotalElements());
        response.put("totalPages", exercisePage.getTotalPages());
        response.put("currentPage", exercisePage.getNumber());
        response.put("size", exercisePage.getSize());

        return response;
    }

    @PostMapping("/fetch")
    public String fetchExercises() {
        exerciseService.fetchAndSaveExercises();
        return "Exercises fetched and saved!";
    }

    @GetMapping("/categories/bodyparts")
    public List<String> getBodyPartList() {
        return exerciseService.getBodyPartList();
    }

    @GetMapping("/categories/equipment")
    public List<String> getEquipmentList() {
        return exerciseService.getEquipmentList();
    }

    @GetMapping("/categories/targets")
    public List<String> getTargetList() {
        return exerciseService.getTargetList();
    }

    @GetMapping("/search")
    public Page<Exercise> searchExercises(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bodyPart,
            @RequestParam(required = false) String equipment,
            Pageable pageable) {
        // Lấy tất cả bài tập từ API
        List<Exercise> allExercises = exerciseApiService.getExercisesFromApi();

        // Lọc dữ liệu theo các tiêu chí
        List<Exercise> filteredExercises = allExercises.stream()
                .filter(e -> (name == null || e.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(e -> (bodyPart == null || e.getBodyPart().equalsIgnoreCase(bodyPart)))
                .filter(e -> (equipment == null || e.getEquipment().equalsIgnoreCase(equipment)))
                .collect(Collectors.toList());

        // Áp dụng phân trang
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredExercises.size());
        List<Exercise> pagedList = filteredExercises.subList(start, end);

        return new PageImpl<>(pagedList, pageable, filteredExercises.size());
    }

    /**
     * Endpoint for exercises filtered by equipment with pagination
     * Returns a PageResponse with content and pagination metadata
     */
    @GetMapping("/search-by-equipment")
    public Map<String, Object> getExercisesByEquipment(
            @RequestParam(name = "equipment") String equipment,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exercise> exercisePage = exerciseService.getExercisesByEquipment(equipment, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", exercisePage.getContent());
        response.put("totalElements", exercisePage.getTotalElements());
        response.put("totalPages", exercisePage.getTotalPages());
        response.put("currentPage", exercisePage.getNumber());
        response.put("size", exercisePage.getSize());

        return response;
    }

    /**
     * Endpoint for exercises filtered by target muscle with pagination
     * Returns a PageResponse with content and pagination metadata
     */
    @GetMapping("/search-by-target")
    public Map<String, Object> getExercisesByTarget(
            @RequestParam(name = "target") String target,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exercise> exercisePage = exerciseService.getExercisesByTarget(target, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", exercisePage.getContent());
        response.put("totalElements", exercisePage.getTotalElements());
        response.put("totalPages", exercisePage.getTotalPages());
        response.put("currentPage", exercisePage.getNumber());
        response.put("size", exercisePage.getSize());

        return response;
    }
}
