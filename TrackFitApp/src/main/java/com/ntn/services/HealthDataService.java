package com.ntn.services;

import com.ntn.dto.HealthDataDTO;
import com.ntn.pojo.HealthData;
import java.util.List;

public interface HealthDataService {
    void create(String username, HealthDataDTO dto);
    List<HealthDataDTO> listByUsername(String username);
    void update(String username, Integer healthId, HealthDataDTO dto);
    void delete(String username, Integer healthId);
    HealthData getLatestEntity(String username);
}
