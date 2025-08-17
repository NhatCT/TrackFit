package com.ntn.repositories;

import com.ntn.pojo.UserWorkoutHistory;
import java.util.Date;
import java.util.List;

public interface UserWorkoutHistoryRepository {
    UserWorkoutHistory save(UserWorkoutHistory h);
    UserWorkoutHistory findById(Integer id);
    void delete(UserWorkoutHistory h);

    // Lọc cơ bản
    List<UserWorkoutHistory> findByUserIdFiltered(Integer userId, Integer planId, Integer exerciseId, String status);

    // Phân trang
    List<UserWorkoutHistory> findByUserIdPaged(Integer userId, Integer planId, Integer exerciseId, String status, int page, int pageSize);
    long countByUserId(Integer userId, Integer planId, Integer exerciseId, String status);

    // Cho AI + thống kê
    long countCompletedBetween(Integer userId, Date from, Date to);
    List<UserWorkoutHistory> findBetween(Integer userId, Date from, Date to, String status);
    List<UserWorkoutHistory> findBetweenAll(Date from, Date to, String status);
}
