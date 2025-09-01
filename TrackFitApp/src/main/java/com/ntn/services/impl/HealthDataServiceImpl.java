package com.ntn.services.impl;

import com.ntn.dto.HealthDataDTO;
import com.ntn.pojo.HealthData;
import com.ntn.pojo.User;
import com.ntn.repositories.HealthDataRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.HealthDataService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HealthDataServiceImpl implements HealthDataService {

    @Autowired private UserRepository userRepo;
    @Autowired private HealthDataRepository healthRepo;

    @Override
    public void create(String username, HealthDataDTO dto) {
        User user = mustGetUser(username);

        HealthData h = new HealthData();
        h.setUserId(user);
        h.setHeight(dto.getHeight() != null ? new BigDecimal(dto.getHeight()) : null);
        h.setWeight(dto.getWeight() != null ? new BigDecimal(dto.getWeight()) : null);
        h.setBloodPressure(dto.getBloodPressure());  // ➕
        h.setNotes(dto.getNotes());                  // ➕
        h.setCreatedAt(new Date());                  // ➕
        h.setUpdatedAt(new Date());
        healthRepo.saveHealthData(h);

        // (tuỳ chọn) đồng bộ 1 số trường sang user
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.updateUser(user);
    }

    @Override
    public List<HealthDataDTO> listByUsername(String username) {
        User user = mustGetUser(username);
        return healthRepo.findByUserId(user.getUserId()).stream().map(h -> {
            HealthDataDTO d = new HealthDataDTO();
            d.setHealthId(h.getHealthId());                                  // ➕
            d.setHeight(h.getHeight() != null ? h.getHeight().doubleValue() : null);
            d.setWeight(h.getWeight() != null ? h.getWeight().doubleValue() : null);
            d.setBloodPressure(h.getBloodPressure());                        // ➕
            d.setNotes(h.getNotes());                                        // ➕
            d.setCreatedAt(h.getCreatedAt());                                // ➕
            d.setUpdatedAt(h.getUpdatedAt());

            d.setGender(user.getGender());
            d.setBirthDate(user.getBirthDate());
            return d;
        }).sorted(Comparator.comparing(HealthDataDTO::getCreatedAt,
                        Comparator.nullsLast(Date::compareTo)).reversed())
          .collect(Collectors.toList());
    }

    @Override
    public void update(String username, Integer healthId, HealthDataDTO dto) {
        User user = mustGetUser(username);
        HealthData h = mustGetOwnedHealth(user, healthId);

        if (dto.getHeight() != null) h.setHeight(new BigDecimal(dto.getHeight()));
        if (dto.getWeight() != null) h.setWeight(new BigDecimal(dto.getWeight()));
        if (dto.getBloodPressure() != null) h.setBloodPressure(dto.getBloodPressure()); // ➕
        if (dto.getNotes() != null) h.setNotes(dto.getNotes());                         // ➕
        h.setUpdatedAt(new Date());
        healthRepo.saveHealthData(h);

        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.updateUser(user);
    }

    @Override
    public void delete(String username, Integer healthId) {
        User user = mustGetUser(username);
        HealthData h = mustGetOwnedHealth(user, healthId);
        healthRepo.deleteHealthData(h);
    }

    @Override
    public HealthData getLatestEntity(String username) {
        User user = mustGetUser(username);
        return healthRepo.findByUserId(user.getUserId()).stream()
                .sorted(Comparator.comparing(HealthData::getUpdatedAt).reversed())
                .findFirst().orElse(null);
    }

    private User mustGetUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng");
        return u;
    }

    private HealthData mustGetOwnedHealth(User u, Integer id) {
        HealthData h = healthRepo.findById(id);
        if (h == null || !h.getUserId().getUserId().equals(u.getUserId()))
            throw new IllegalArgumentException("Bản ghi không tồn tại hoặc không thuộc về bạn");
        return h;
    }
}
