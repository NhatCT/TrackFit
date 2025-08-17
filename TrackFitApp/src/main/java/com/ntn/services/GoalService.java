package com.ntn.services;

import com.ntn.dto.GoalDTO;
import java.util.List;

public interface GoalService {
    void create(String username, GoalDTO dto);
    List<GoalDTO> listByUsername(String username);
    void update(String username, Integer goalId, GoalDTO dto);
    void delete(String username, Integer goalId);
}
