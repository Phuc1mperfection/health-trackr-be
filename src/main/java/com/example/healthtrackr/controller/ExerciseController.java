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

    @GetMapping("/bodypart/{bodyPart}")
    public List<Exercise> getExercisesByBodyPart(@PathVariable String bodyPart) {
        return exerciseService.getExercisesByBodyPart(bodyPart);
    }

    @PostMapping("/fetch")
    public String fetchExercises() {
        exerciseService.fetchAndSaveExercises();
        return "Exercises fetched and saved!";
    }
    @GetMapping("/bodyparts")
    public List<String> getBodyPartList() {
        return exerciseService.getBodyPartList();
    }
    @GetMapping("/search")
    public Page<Exercise> searchExercises(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bodyPart,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String difficulty,
            Pageable pageable
    ) {
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

}
