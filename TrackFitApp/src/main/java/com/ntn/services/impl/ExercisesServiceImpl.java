package com.ntn.services.impl;

import com.ntn.dto.ExerciseCreateUpdateDTO;
import com.ntn.dto.ExerciseDTO;
import com.ntn.pojo.Exercises;
import com.ntn.repositories.ExercisesRepository;
import com.ntn.services.ExercisesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExercisesServiceImpl implements ExercisesService {

    @Autowired
    private ExercisesRepository repo;

    @Override
    public ExerciseDTO create(ExerciseCreateUpdateDTO req) {
        Exercises e = new Exercises();
        apply(e, req);
        e.setCreatedAt(new Date());
        e = repo.save(e);
        return toDTO(e);
    }

    @Override
    public ExerciseDTO update(Integer id, ExerciseCreateUpdateDTO req) {
        Exercises cur = mustGet(id);
        apply(cur, req);
        cur = repo.save(cur);
        return toDTO(cur);
    }

    @Override
    public ExerciseDTO get(Integer id) {
        return toDTO(mustGet(id));
    }

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize, String kw) {
        int pageNum = (page != null && page > 0) ? page : 1;
        int ps = (pageSize != null && pageSize > 0) ? pageSize : 10;

        Map<String, String> params = new HashMap<>();
        if (kw != null && !kw.isBlank()) {
            params.put("kw", kw.trim());
        }
        params.put("page", String.valueOf(pageNum));
        params.put("pageSize", String.valueOf(ps));

        List<ExerciseDTO> items = repo.getExercises(params).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        long total = repo.countExercises(params);
        int totalPages = (int) Math.ceil(total * 1.0 / ps);

        return Map.of(
            "page", pageNum,
            "pageSize", ps,
            "totalPages", Math.max(totalPages, 1),
            "totalElements", total,
            "items", items
        );
    }

    @Override
    public void delete(Integer id) {
        repo.delete(mustGet(id));
    }

    private Exercises mustGet(Integer id) {
        Exercises ex = repo.findById(id);
        if (ex == null) throw new IllegalArgumentException("Không tìm thấy bài tập");
        return ex;
    }

    private void apply(Exercises e, ExerciseCreateUpdateDTO req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Tên bài tập không được để trống");
        }
        e.setName(req.getName());
        e.setTargetGoal(req.getTargetGoal());
        e.setMuscleGroup(req.getMuscleGroup());
        e.setVideoUrl(req.getVideoUrl());
        e.setDescription(req.getDescription());
    }

    private ExerciseDTO toDTO(Exercises e) {
        ExerciseDTO dto = new ExerciseDTO();
        dto.setExercisesId(e.getExercisesId());
        dto.setName(e.getName());
        dto.setTargetGoal(e.getTargetGoal());
        dto.setMuscleGroup(e.getMuscleGroup());
        dto.setVideoUrl(e.getVideoUrl());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
