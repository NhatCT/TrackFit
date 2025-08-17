/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repositories;

/**
 *
 * @author Thanh Nhat
 */
import com.ntn.pojo.Goal;
import java.util.List;

public interface GoalRepository {
    Goal saveGoal(Goal goal);
    List<Goal> findByUserId(Integer userId);
    Goal findById(Integer goalId);
    void deleteGoal(Goal goal);
}
