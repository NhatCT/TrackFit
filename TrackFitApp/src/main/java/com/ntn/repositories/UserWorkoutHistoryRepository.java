package com.ntn.repositories;

import com.ntn.pojo.UserWorkoutHistory;
import java.util.List;

public interface UserWorkoutHistoryRepository {
    UserWorkoutHistory save(UserWorkoutHistory h);
    UserWorkoutHistory findById(Integer id);
    void delete(UserWorkoutHistory h);

    // Lấy tất cả (không phân trang)
    List<UserWorkoutHistory> findByUserIdFiltered(Integer userId, Integer planId, Integer exerciseId, String status);

    // Phân trang
    List<UserWorkoutHistory> findByUserIdPaged(Integer userId, Integer planId, Integer exerciseId, String status, int page, int pageSize);
    long countByUserId(Integer userId, Integer planId, Integer exerciseId, String status);
}
