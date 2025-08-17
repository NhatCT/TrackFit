package com.ntn.controllers;

import com.ntn.dto.ExerciseCreateUpdateDTO;
import com.ntn.services.ExercisesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/secure/exercises")
@CrossOrigin
public class ApiExerciseController {

    @Autowired
    private ExercisesService exercisesService;

    @GetMapping
    public ResponseEntity<?> listPaged(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "kw", required = false) String kw) {
        return ResponseEntity.ok(exercisesService.list(page, pageSize, kw));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(exercisesService.get(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody ExerciseCreateUpdateDTO req) {
        return ResponseEntity.ok(exercisesService.create(req));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable("id") Integer id,
            @Valid @RequestBody ExerciseCreateUpdateDTO req) {
        return ResponseEntity.ok(exercisesService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id) {
        exercisesService.delete(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Xóa bài tập thành công"));
    }
}
