// src/main/java/com/ntn/repositories/UserWorkoutHistoryRepository.java
package com.ntn.repositories;

import com.ntn.dto.ExerciseShare;
import com.ntn.pojo.UserWorkoutHistory;
import java.util.Date;
import java.util.List;

public interface UserWorkoutHistoryRepository {
    List<UserWorkoutHistory> findBetweenAll(Date from, Date to, String status);
    UserWorkoutHistory save(UserWorkoutHistory h);
    UserWorkoutHistory findById(Integer id);
    void delete(UserWorkoutHistory h);
    List<UserWorkoutHistory> findByUserIdFiltered(Integer userId, Integer planId, Integer exerciseId, String status);
    List<UserWorkoutHistory> findByUserIdPaged(Integer userId, Integer planId, Integer exerciseId, String status, int page, int pageSize);
    long countByUserId(Integer userId, Integer planId, Integer exerciseId, String status);
    long countCompletedBetween(Integer userId, Date from, Date to);
    List<UserWorkoutHistory> findBetween(Integer userId, Date from, Date to, String status);
List<ExerciseShare> countByExercise(Integer userId, Date from, Date toExclusive);
    List<Integer> findRecentExerciseIds(Integer userId, int limit);
}
