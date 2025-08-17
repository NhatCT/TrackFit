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
        // Lấy tất cả rồi lọc/sort/cắt trang ở service (đơn giản, linh hoạt)
        List<ExerciseDTO> all = repo.findAll().stream()
                .filter(e -> kw == null || kw.isBlank()
                        || (e.getName() != null && e.getName().toLowerCase().contains(kw.toLowerCase())))
                .sorted(Comparator.comparing(Exercises::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toDTO)
                .collect(Collectors.toList());

        if (page == null || pageSize == null) {
            return Map.of(
                "page", null,
                "pageSize", null,
                "totalPages", 1,
                "totalElements", all.size(),
                "items", all
            );
        }

        int p = Math.max(page, 1);
        int ps = Math.max(pageSize, 1);
        int total = all.size();
        int totalPages = (int) Math.ceil(total * 1.0 / ps);
        int start = (p - 1) * ps;
        int end = Math.min(start + ps, total);
        List<ExerciseDTO> items = (start >= total) ? List.of() : all.subList(start, end);

        Map<String, Object> res = new HashMap<>();
        res.put("page", p);
        res.put("pageSize", ps);
        res.put("totalPages", totalPages);
        res.put("totalElements", total);
        res.put("items", items);
        return res;
    }

    @Override
    public void delete(Integer id) {
        repo.delete(mustGet(id));
    }

    // ===== helpers =====
    private Exercises mustGet(Integer id) {
        Exercises ex = repo.findById(id);
        if (ex == null) throw new IllegalArgumentException("Không tìm thấy bài tập");
        return ex;
    }

    private void apply(Exercises e, ExerciseCreateUpdateDTO req) {
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
