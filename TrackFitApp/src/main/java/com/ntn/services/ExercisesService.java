package com.ntn.services;

import com.ntn.dto.ExerciseCreateUpdateDTO;
import com.ntn.dto.ExerciseDTO;
import java.util.Map;

public interface ExercisesService {
    ExerciseDTO create(ExerciseCreateUpdateDTO req);
    ExerciseDTO update(Integer id, ExerciseCreateUpdateDTO req);
    ExerciseDTO get(Integer id);
    Map<String, Object> list(Integer page, Integer pageSize, String kw);

    void delete(Integer id);
}
