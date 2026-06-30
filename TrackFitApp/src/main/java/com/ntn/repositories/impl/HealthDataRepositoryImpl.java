package com.ntn.repositories.impl;

import com.ntn.pojo.HealthData;
import com.ntn.repositories.HealthDataRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HealthDataRepositoryImpl extends BaseHibernateRepository implements HealthDataRepository {

    @Override
    public HealthData saveHealthData(HealthData healthData) {
        return saveOrMerge(healthData, healthData.getHealthId());
    }

    @Override
    public List<HealthData> findByUserId(Integer userId) {
        var s = currentSession();
        Query q = s.createNamedQuery("HealthData.findByUserId", HealthData.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public HealthData findById(Integer healthId) {
        var s = currentSession();
        Query q = s.createNamedQuery("HealthData.findByHealthId", HealthData.class);
        q.setParameter("healthId", healthId);
        try {
            return (HealthData) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteHealthData(HealthData healthData) {
        removeEntity(healthData);
    }
}