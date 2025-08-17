package com.ntn.services;

import com.ntn.dto.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService extends UserDetailsService {

    UserResponseDTO getUserByUsername(String username);

    UserResponseDTO getUserByEmail(String email);

    UserResponseDTO register(UserRegistrationDTO dto, MultipartFile avatar);

    boolean authenticate(String username, String password);

    void changePassword(String username, String oldPassword, String newPassword);

    UserResponseDTO updateAvatar(String username, MultipartFile avatar);

    UserResponseDTO getUserWithHealthInfo(String username);

    boolean updateHealthMetrics(String username, HealthDataDTO healthData);
//
//    List<HealthDataDTO> getHealthDataByUsername(String username);
//
//    void updateHealthData(String username, Integer healthId, HealthDataDTO healthData);
//
//    void deleteHealthData(String username, Integer healthId);

//    void createGoal(String username, GoalDTO goal);
//
//    List<GoalDTO> getGoalsByUsername(String username);
//
//    void updateGoal(String username, Integer goalId, GoalDTO goal);
//
//    void deleteGoal(String username, Integer goalId);
}
