package com.ntn.services.impl;

import com.ntn.dto.GoalDTO;
import com.ntn.pojo.Goal;
import com.ntn.pojo.User;
import com.ntn.repositories.GoalRepository;
import com.ntn.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Import thêm cái này để bắt dữ liệu
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private GoalRepository goalRepo;

    @InjectMocks
    private GoalServiceImpl goalService;

    private User user;
    private Goal goal;
    private GoalDTO goalDTO;

    @BeforeEach
    void setUp() {
        // Setup dữ liệu mẫu
        user = new User();
        user.setUserId(1);
        user.setUsername("testuser");

        goal = new Goal();
        goal.setGoalId(10);
        goal.setUserId(user); 
        goal.setGoalType("Weight Loss");
        goal.setWorkoutDuration(30);
        goal.setIntensity("High");

        goalDTO = new GoalDTO();
        goalDTO.setGoalType("Muscle Gain");
        goalDTO.setWorkoutDuration(60);
        goalDTO.setIntensity("Medium");
    }

    // --- TEST CREATE ---

    @Test
    @DisplayName("Create: Success - Save goal and check system fields")
    void create_ShouldSaveGoal_AndSetSystemFields() {
        // GIVEN
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);

        // WHEN
        goalService.create("testuser", goalDTO);

        // THEN
        // Dùng ArgumentCaptor để bắt lấy object được gửi xuống DB
        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepo).saveGoal(goalCaptor.capture());

        Goal capturedGoal = goalCaptor.getValue();
        // Kiểm tra kỹ dữ liệu hệ thống tự tạo
        assertNotNull(capturedGoal.getCreatedAt(), "Ngày tạo phải được set");
        assertEquals(user, capturedGoal.getUserId(), "User phải được gán đúng");
        assertEquals("Muscle Gain", capturedGoal.getGoalType());
    }

    @Test
    @DisplayName("Create: Fail - Throw Exception when user not found")
    void create_ShouldThrow_WhenUserNotFound() {
        when(userRepo.getUserByUsername("unknown")).thenReturn(null);
        
        Exception ex = assertThrows(IllegalArgumentException.class, () -> 
            goalService.create("unknown", goalDTO)
        );
        assertEquals("Không tìm thấy người dùng", ex.getMessage());
    }

    // --- TEST LIST ---

    @Test
    @DisplayName("List: Success - Return formatted string properly")
    void listByUsername_ShouldReturnFormattedDTOs() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(goalRepo.findByUserId(1)).thenReturn(Collections.singletonList(goal));

        List<GoalDTO> result = goalService.listByUsername("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        // Kiểm tra format: "Weight Loss • 30p • High"
        assertEquals("Weight Loss • 30p • High", result.get(0).getName());
    }

    @Test
    @DisplayName("List: Success - Handle NULL fields in formatting")
    void listByUsername_ShouldHandleNullFields() {
        // GIVEN: Goal bị thiếu Duration và Intensity
        Goal emptyGoal = new Goal();
        emptyGoal.setGoalId(20);
        emptyGoal.setUserId(user);
        emptyGoal.setGoalType("Yoga"); // Các trường khác null

        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(goalRepo.findByUserId(1)).thenReturn(Collections.singletonList(emptyGoal));

        // WHEN
        List<GoalDTO> result = goalService.listByUsername("testuser");

        // THEN: Chuỗi hiển thị chỉ là "Yoga", không có dấu chấm thừa
        assertEquals("Yoga", result.get(0).getName());
    }

    // --- TEST UPDATE ---

    @Test
    @DisplayName("Update: Success - Update fields correctly")
    void update_ShouldUpdate_WhenOwnerMatches() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(goalRepo.findById(10)).thenReturn(goal);

        goalService.update("testuser", 10, goalDTO);

        assertEquals("Muscle Gain", goal.getGoalType());
        assertEquals(60, goal.getWorkoutDuration());
        verify(goalRepo).saveGoal(goal);
    }

    @Test
    @DisplayName("Update: Fail - Throw Exception when hacking other user's goal")
    void update_ShouldThrow_WhenAccessDenied() {
        User hacker = new User();
        hacker.setUserId(99); 
        hacker.setUsername("hacker");

        when(userRepo.getUserByUsername("hacker")).thenReturn(hacker);
        when(goalRepo.findById(10)).thenReturn(goal); // Goal của User 1

        Exception ex = assertThrows(IllegalArgumentException.class, () -> 
            goalService.update("hacker", 10, goalDTO)
        );
        assertEquals("Mục tiêu không tồn tại hoặc không thuộc về bạn", ex.getMessage());
        verify(goalRepo, never()).saveGoal(any());
    }

    @Test
    @DisplayName("Update: Partial - Keep old values if DTO fields are null")
    void update_ShouldOnlyUpdateNonNullFields() {
        GoalDTO partialDTO = new GoalDTO();
        partialDTO.setWorkoutDuration(120); // Chỉ update thời gian

        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(goalRepo.findById(10)).thenReturn(goal);

        goalService.update("testuser", 10, partialDTO);

        assertEquals(120, goal.getWorkoutDuration()); // Đổi
        assertEquals("Weight Loss", goal.getGoalType()); // Giữ nguyên
        verify(goalRepo).saveGoal(goal);
    }

    // --- TEST DELETE ---

    @Test
    @DisplayName("Delete: Success - Delete correctly")
    void delete_ShouldCallDelete_WhenOwnerMatches() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(goalRepo.findById(10)).thenReturn(goal);

        goalService.delete("testuser", 10);

        verify(goalRepo).deleteGoal(goal);
    }

    @Test
    @DisplayName("Delete: Fail - Security Check (Cannot delete other's goal)")
    void delete_ShouldThrow_WhenAccessDenied() {
        User hacker = new User();
        hacker.setUserId(99);
        hacker.setUsername("hacker");

        when(userRepo.getUserByUsername("hacker")).thenReturn(hacker);
        when(goalRepo.findById(10)).thenReturn(goal);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> 
            goalService.delete("hacker", 10)
        );
        assertEquals("Mục tiêu không tồn tại hoặc không thuộc về bạn", ex.getMessage());
        verify(goalRepo, never()).deleteGoal(any());
    }
}
