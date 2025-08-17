package com.ntn.repositories.impl;

import com.ntn.pojo.Goal;
import com.ntn.repositories.GoalRepository;
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
public class GoalRepositoryImpl implements GoalRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Goal saveGoal(Goal goal) {
        Session s = this.factory.getObject().getCurrentSession();
        if (goal.getGoalId() == null) {
            s.persist(goal);
        } else {
            goal = (Goal) s.merge(goal);
        }
        return goal;
    }

    @Override
    public List<Goal> findByUserId(Integer userId) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createNamedQuery("Goal.findByUserId", Goal.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public Goal findById(Integer goalId) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createNamedQuery("Goal.findByGoalId", Goal.class);
        q.setParameter("goalId", goalId);
        try {
            return (Goal) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void deleteGoal(Goal goal) {
        Session s = this.factory.getObject().getCurrentSession();
        s.delete(s.contains(goal) ? goal : s.merge(goal));
    }
}