package com.ntn.services;

import com.ntn.dto.HistoryCreateUpdateDTO;
import com.ntn.dto.HistoryDTO;

import java.util.Map;

public interface UserWorkoutHistoryService {
    HistoryDTO create(String username, HistoryCreateUpdateDTO req);
    HistoryDTO get(String username, Integer id);
    HistoryDTO update(String username, Integer id, HistoryCreateUpdateDTO req);
    void delete(String username, Integer id);

    Map<String, Object> listByUserPaged(String username, Integer page, Integer pageSize,
                                        Integer planId, Integer exerciseId, String status);
}
