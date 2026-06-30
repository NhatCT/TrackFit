package com.ntn.repositories.impl;

import com.ntn.pojo.Goal;
import com.ntn.repositories.GoalRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class GoalRepositoryImpl extends BaseHibernateRepository implements GoalRepository {

    @Override
    public Goal saveGoal(Goal goal) {
        return saveOrMerge(goal, goal.getGoalId());
    }

    @Override
    public List<Goal> findByUserId(Integer userId) {
        var s = currentSession();
        Query q = s.createNamedQuery("Goal.findByUserId", Goal.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public Goal findById(Integer goalId) {
        var s = currentSession();
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
        removeEntity(goal);
    }
}