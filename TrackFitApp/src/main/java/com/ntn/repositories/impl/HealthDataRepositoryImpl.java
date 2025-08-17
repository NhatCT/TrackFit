package com.ntn.repositories.impl;

import com.ntn.pojo.HealthData;
import com.ntn.repositories.HealthDataRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class HealthDataRepositoryImpl implements HealthDataRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public HealthData saveHealthData(HealthData healthData) {
        Session s = this.factory.getObject().getCurrentSession();
        if (healthData.getHealthId() == null) {
            s.persist(healthData);
        } else {
            healthData = (HealthData) s.merge(healthData);
        }
        return healthData;
    }

    @Override
    public List<HealthData> findByUserId(Integer userId) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createNamedQuery("HealthData.findByUserId", HealthData.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public HealthData findById(Integer healthId) {
        Session s = this.factory.getObject().getCurrentSession();
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
        Session s = this.factory.getObject().getCurrentSession();
        s.delete(s.contains(healthData) ? healthData : s.merge(healthData));
    }
}