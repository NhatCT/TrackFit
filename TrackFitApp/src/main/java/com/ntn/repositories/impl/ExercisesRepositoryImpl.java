package com.ntn.repositories.impl;

import com.ntn.pojo.Exercises;
import com.ntn.repositories.ExercisesRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.ntn.utils.PaginationHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ExercisesRepositoryImpl extends BaseHibernateRepository implements ExercisesRepository {

    @Override
    public long countAll() {
        var s = currentSession();
        var cb = s.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(Exercises.class);
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public Exercises save(Exercises e) {
        return saveOrMerge(e, e.getExercisesId());
    }

    @Override
    public Exercises findById(Integer id) {
        return currentSession().get(Exercises.class, id);
    }

    @Override
    public List<Exercises> findAll() {
        var s = currentSession();
        TypedQuery<Exercises> q = s.createNamedQuery("Exercises.findAll", Exercises.class);
        return q.getResultList();
    }

    @Override
    public void delete(Exercises e) {
        removeEntity(e);
    }

    @Override
    public List<Exercises> getExercises(Map<String, String> params) {
        var s = currentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Exercises> cq = cb.createQuery(Exercises.class);
        Root<Exercises> root = cq.from(Exercises.class);

        List<Predicate> predicates = new ArrayList<>();

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + kw.trim().toLowerCase() + "%"));
            // Optional: mở rộng tìm theo muscleGroup/targetGoal
            // String p = "%" + kw.trim().toLowerCase() + "%";
            // predicates.add(cb.or(
            //     cb.like(cb.lower(root.get("name")), p),
            //     cb.like(cb.lower(root.get("muscleGroup")), p),
            //     cb.like(cb.lower(root.get("targetGoal")), p)
            // ));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(Predicate[]::new));
        }

        try {
            cq.orderBy(cb.desc(root.get("createdAt")));
        } catch (Exception ignore) {
            cq.orderBy(cb.desc(root.get("exercisesId")));
        }

        TypedQuery<Exercises> q = s.createQuery(cq);

        int page = PaginationHelper.parseParam(params, "page", 1);
        int pageSize = PaginationHelper.parseParam(params, "pageSize", 10);

        int start = (page - 1) * pageSize;
        q.setFirstResult(start);
        q.setMaxResults(pageSize);

        return q.getResultList();
    }

    @Override
    public long countExercises(Map<String, String> params) {
        var s = currentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Exercises> root = cq.from(Exercises.class);

        List<Predicate> predicates = new ArrayList<>();

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + kw.trim().toLowerCase() + "%"));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(Predicate[]::new));
        }

        cq.select(cb.count(root));
        TypedQuery<Long> q = s.createQuery(cq);
        return q.getSingleResult();
    }
}
