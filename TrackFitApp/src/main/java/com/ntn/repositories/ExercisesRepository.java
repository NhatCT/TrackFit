package com.ntn.repositories;

import com.ntn.pojo.Exercises;
import java.util.List;
import java.util.Map;

public interface ExercisesRepository {
    Exercises save(Exercises e);
    Exercises findById(Integer id);
    List<Exercises> findAll();
    void delete(Exercises e);
    List<Exercises> getExercises(Map<String,String> params);
    long countExercises(Map<String,String> params);
}

