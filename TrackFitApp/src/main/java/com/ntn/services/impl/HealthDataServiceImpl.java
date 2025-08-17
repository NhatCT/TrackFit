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
        h.setUpdatedAt(new Date());
        healthRepo.saveHealthData(h);

        // (Tuỳ chọn) đồng bộ field tĩnh từ form vào bảng user
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
            d.setHeight(h.getHeight() != null ? h.getHeight().doubleValue() : null);
            d.setWeight(h.getWeight() != null ? h.getWeight().doubleValue() : null);
            d.setGender(user.getGender());          // lấy từ bảng user
            d.setBirthDate(user.getBirthDate());    // lấy từ bảng user
            // Không set updatedAt vì DTO không có field này
            // Không set goalType/workoutDays vì không nằm trong health_data (tuỳ form gửi vào)
            return d;
        }).collect(Collectors.toList());
    }

    @Override
    public void update(String username, Integer healthId, HealthDataDTO dto) {
        User user = mustGetUser(username);
        HealthData h = mustGetOwnedHealth(user, healthId);

        h.setHeight(dto.getHeight() != null ? new BigDecimal(dto.getHeight()) : null);
        h.setWeight(dto.getWeight() != null ? new BigDecimal(dto.getWeight()) : null);
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
        List<HealthData> list = healthRepo.findByUserId(user.getUserId());
        return list.stream().sorted(Comparator.comparing(HealthData::getUpdatedAt).reversed())
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


