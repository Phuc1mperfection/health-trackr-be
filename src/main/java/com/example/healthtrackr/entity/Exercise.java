package com.example.healthtrackr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String bodyPart;
    private String target;
    private String equipment;
    private String gifUrl;
    private List<String> instructions;
    private List<String> secondaryMuscles; // ➜ Thêm thuộc tính này


}
