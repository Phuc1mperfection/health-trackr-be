package com.example.healthtrackr.controller;

import com.example.healthtrackr.entity.Exercise;
import com.example.healthtrackr.service.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
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
    @GetMapping("/exercises/search")
    public ResponseEntity<Page<Exercise>> searchExercises(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bodyPart,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exercise> exercises = exerciseService.searchExercises(name, bodyPart, equipment, difficulty, pageable);
        return ResponseEntity.ok(exercises);
    }

}
