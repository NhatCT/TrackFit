package com.ntn.services.impl;

import com.ntn.dto.GoalDTO;
import com.ntn.pojo.Goal;
import com.ntn.pojo.User;
import com.ntn.repositories.GoalRepository;
import com.ntn.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private GoalRepository goalRepo;

    @InjectMocks
    private GoalServiceImpl goalService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
    }

    @Test
    void create_validDto_savesGoal() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(testUser);

        GoalDTO dto = new GoalDTO();
        dto.setGoalType("muscle_gain");
        dto.setWorkoutDuration(45);
        dto.setIntensity("High");

        goalService.create("testuser", dto);

        ArgumentCaptor<Goal> captor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepo).saveGoal(captor.capture());

        Goal saved = captor.getValue();
        assertEquals("muscle_gain", saved.getGoalType());
        assertEquals(45, saved.getWorkoutDuration());
        assertEquals("High", saved.getIntensity());
        assertNotNull(saved.getCreatedAt());
        assertEquals(testUser, saved.getUserId());
    }

    @Test
    void create_userNotFound_throwsException() {
        when(userRepo.getUserByUsername("unknown")).thenReturn(null);

        GoalDTO dto = new GoalDTO();
        dto.setGoalType("weight_loss");

        assertThrows(IllegalArgumentException.class,
                () -> goalService.create("unknown", dto));
    }

    @Test
    void listByUsername_returnsFormattedGoals() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(testUser);

        Goal g = new Goal();
        g.setGoalId(10);
        g.setGoalType("weight_loss");
        g.setWorkoutDuration(30);
        g.setIntensity("Medium");
        g.setUserId(testUser);

        when(goalRepo.findByUserId(1)).thenReturn(List.of(g));

        List<GoalDTO> results = goalService.listByUsername("testuser");

        assertEquals(1, results.size());
        GoalDTO result = results.get(0);
        assertEquals(10, result.getGoalId());
        assertEquals("weight_loss", result.getGoalType());
        assertEquals(30, result.getWorkoutDuration());
        assertEquals("Medium", result.getIntensity());
        assertTrue(result.getName().contains("weight_loss"));
    }

    @Test
    void update_validGoal_updatesFields() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(testUser);

        Goal existing = new Goal();
        existing.setGoalId(10);
        existing.setGoalType("old");
        existing.setUserId(testUser);
        when(goalRepo.findById(10)).thenReturn(existing);

        GoalDTO dto = new GoalDTO();
        dto.setGoalType("weight_loss");
        dto.setIntensity("High");

        goalService.update("testuser", 10, dto);

        verify(goalRepo).saveGoal(existing);
        assertEquals("weight_loss", existing.getGoalType());
        assertEquals("High", existing.getIntensity());
    }

    @Test
    void update_goalNotOwned_throwsException() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(testUser);

        User otherUser = new User();
        otherUser.setUserId(99);

        Goal otherGoal = new Goal();
        otherGoal.setGoalId(20);
        otherGoal.setUserId(otherUser);
        when(goalRepo.findById(20)).thenReturn(otherGoal);

        GoalDTO dto = new GoalDTO();
        dto.setGoalType("something");

        assertThrows(IllegalArgumentException.class,
                () -> goalService.update("testuser", 20, dto));
    }

    @Test
    void delete_validGoal_deletesGoal() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(testUser);

        Goal g = new Goal();
        g.setGoalId(10);
        g.setUserId(testUser);
        when(goalRepo.findById(10)).thenReturn(g);

        goalService.delete("testuser", 10);
        verify(goalRepo).deleteGoal(g);
    }

    @Test
    void delete_goalNotFound_throwsException() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(testUser);
        when(goalRepo.findById(999)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> goalService.delete("testuser", 999));
    }
}
