/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repositories;

/**
 *
 * @author Thanh Nhat
 */
import com.ntn.pojo.HealthData;
import java.util.List;

public interface HealthDataRepository {
    HealthData saveHealthData(HealthData healthData);
    List<HealthData> findByUserId(Integer userId);
    HealthData findById(Integer healthId);
    void deleteHealthData(HealthData healthData);
}
