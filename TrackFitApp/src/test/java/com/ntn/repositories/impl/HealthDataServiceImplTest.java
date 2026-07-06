package com.ntn.repositories.impl;

import com.ntn.dto.HealthDataDTO;
import com.ntn.pojo.HealthData;
import com.ntn.pojo.User;
import com.ntn.repositories.HealthDataRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.impl.HealthDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthDataServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private HealthDataRepository healthRepo;

    @InjectMocks
    private HealthDataServiceImpl healthService;

    private User user;
    private HealthData healthData;
    private HealthDataDTO healthDTO;

    @BeforeEach
    void setUp() {
        // Setup User
        user = new User();
        user.setUserId(1);
        user.setUsername("testuser");
        user.setGender("Male");

        // Setup HealthData Entity
        healthData = new HealthData();
        healthData.setHealthId(100);
        healthData.setUserId(user);
        healthData.setHeight(new BigDecimal("175.5"));
        healthData.setWeight(new BigDecimal("70.0"));
        healthData.setCreatedAt(new Date());
        healthData.setUpdatedAt(new Date());

        // Setup DTO
        healthDTO = new HealthDataDTO();
        healthDTO.setHeight(180.0);
        healthDTO.setWeight(75.0);
        healthDTO.setBloodPressure("120/80");
        healthDTO.setGender("Female"); // Giả lập thay đổi giới tính
        healthDTO.setNotes("Feeling good");
    }

    // --- TEST CREATE ---

    @Test
    @DisplayName("Create: Success - Save HealthData AND Update User info")
    void create_ShouldSaveHealthAndUser() {
        // GIVEN
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);

        // WHEN
        healthService.create("testuser", healthDTO);

        // THEN
        // 1. Kiểm tra HealthData được lưu đúng
        ArgumentCaptor<HealthData> healthCaptor = ArgumentCaptor.forClass(HealthData.class);
        verify(healthRepo).saveHealthData(healthCaptor.capture());
        
        HealthData savedHealth = healthCaptor.getValue();
        assertEquals(0, new BigDecimal("180.0").compareTo(savedHealth.getHeight())); // Check conversion Double -> BigDecimal
        assertNotNull(savedHealth.getCreatedAt());
        assertEquals("120/80", savedHealth.getBloodPressure());

        // 2. Kiểm tra User được cập nhật (Gender, UpdatedAt)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).updateUser(userCaptor.capture());
        
        User updatedUser = userCaptor.getValue();
        assertEquals("Female", updatedUser.getGender()); // User đổi từ Male -> Female
        assertNotNull(updatedUser.getUpdatedAt()); // Thời gian update user phải được set
    }

    @Test
    @DisplayName("Create: Fail - Throw Exception when User not found")
    void create_ShouldThrow_WhenUserNotFound() {
        when(userRepo.getUserByUsername("ghost")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> 
            healthService.create("ghost", healthDTO)
        );
        verify(healthRepo, never()).saveHealthData(any());
    }

    // --- TEST LIST ---

    @Test
    @DisplayName("List: Success - Return DTOs containing User info & Sorted by Date Desc")
    void listByUsername_ShouldReturnSortedDTOs() {
        // GIVEN
        // Tạo 2 record, record 2 mới hơn record 1
        HealthData h1 = new HealthData();
        h1.setCreatedAt(new Date(100000)); // Cũ
        h1.setHeight(BigDecimal.TEN);
        
        HealthData h2 = new HealthData();
        h2.setCreatedAt(new Date(200000)); // Mới
        h2.setHeight(BigDecimal.ONE);

        List<HealthData> list = Arrays.asList(h1, h2);

        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(healthRepo.findByUserId(1)).thenReturn(list);

        // WHEN
        List<HealthDataDTO> result = healthService.listByUsername("testuser");

        // THEN
        assertEquals(2, result.size());
        
        // Kiểm tra Sorting: Mới nhất (h2) lên đầu
        assertEquals(h2.getHeight().doubleValue(), result.get(0).getHeight());
        assertEquals(h1.getHeight().doubleValue(), result.get(1).getHeight());

        // Kiểm tra thông tin User được map vào DTO
        assertEquals("Male", result.get(0).getGender());
    }

    // --- TEST UPDATE ---

    @Test
    @DisplayName("Update: Success - Update fields & User info when Owner matches")
    void update_ShouldUpdate_WhenOwnerMatches() {
        // GIVEN
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(healthRepo.findById(100)).thenReturn(healthData);

        // WHEN
        healthService.update("testuser", 100, healthDTO);

        // THEN
        // Kiểm tra trường được update
        assertEquals(0, new BigDecimal("180.0").compareTo(healthData.getHeight()));
        assertEquals("120/80", healthData.getBloodPressure());
        
        // Kiểm tra User update
        verify(userRepo).updateUser(user);
        assertEquals("Female", user.getGender());
    }

    @Test
    @DisplayName("Update: Partial - Only update non-null fields")
    void update_ShouldPartialUpdate() {
        // GIVEN
        HealthDataDTO partialDTO = new HealthDataDTO();
        partialDTO.setWeight(90.0); // Chỉ update cân nặng
        // Height null, BloodPressure null

        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(healthRepo.findById(100)).thenReturn(healthData);

        // WHEN
        healthService.update("testuser", 100, partialDTO);

        // THEN
        assertEquals(0, new BigDecimal("90.0").compareTo(healthData.getWeight())); // Update
        assertEquals(0, new BigDecimal("175.5").compareTo(healthData.getHeight())); // Giữ nguyên
        verify(healthRepo).saveHealthData(healthData);
    }

    @Test
    @DisplayName("Update: Fail - Security Check (Access Denied)")
    void update_ShouldThrow_WhenAccessDenied() {
        User hacker = new User();
        hacker.setUserId(99);
        hacker.setUsername("hacker");

        when(userRepo.getUserByUsername("hacker")).thenReturn(hacker);
        when(healthRepo.findById(100)).thenReturn(healthData); // Data này của User ID 1

        Exception ex = assertThrows(IllegalArgumentException.class, () -> 
            healthService.update("hacker", 100, healthDTO)
        );
        assertEquals("Bản ghi không tồn tại hoặc không thuộc về bạn", ex.getMessage());
    }

    // --- TEST DELETE ---

    @Test
    @DisplayName("Delete: Success - Delete when Owner matches")
    void delete_ShouldDelete_WhenOwnerMatches() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(healthRepo.findById(100)).thenReturn(healthData);

        healthService.delete("testuser", 100);

        verify(healthRepo).deleteHealthData(healthData);
    }

    // --- TEST GET LATEST ---

    @Test
    @DisplayName("GetLatest: Success - Return record with most recent UpdatedAt")
    void getLatestEntity_ShouldReturnMostRecent() {
        // GIVEN
        HealthData oldData = new HealthData();
        oldData.setUpdatedAt(new Date(1000));
        
        HealthData newData = new HealthData();
        newData.setUpdatedAt(new Date(9000)); // Mới nhất

        List<HealthData> list = Arrays.asList(oldData, newData);

        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(healthRepo.findByUserId(1)).thenReturn(list);

        // WHEN
        HealthData result = healthService.getLatestEntity("testuser");

        // THEN
        assertNotNull(result);
        assertSame(newData, result);
    }
    
    @Test
    @DisplayName("GetLatest: Return null if list is empty")
    void getLatestEntity_ShouldReturnNull_WhenEmpty() {
        when(userRepo.getUserByUsername("testuser")).thenReturn(user);
        when(healthRepo.findByUserId(1)).thenReturn(Collections.emptyList());

        HealthData result = healthService.getLatestEntity("testuser");

        assertNull(result);
    }
}
