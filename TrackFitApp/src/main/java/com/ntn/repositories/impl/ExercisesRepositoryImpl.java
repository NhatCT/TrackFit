package com.ntn.repositories.impl;

import com.ntn.pojo.Exercises;
import com.ntn.repositories.ExercisesRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ExercisesRepositoryImpl implements ExercisesRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public long countAll() {
        Session s = factory.getObject().getCurrentSession();
        var cb = s.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(Exercises.class);
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public Exercises save(Exercises e) {
        Session s = factory.getObject().getCurrentSession();
        if (e.getExercisesId() == null) {
            s.persist(e);
            return e;
        }
        return (Exercises) s.merge(e);
    }

    @Override
    public Exercises findById(Integer id) {
        return factory.getObject().getCurrentSession().get(Exercises.class, id);
    }

    @Override
    public List<Exercises> findAll() {
        Session s = factory.getObject().getCurrentSession();
        TypedQuery<Exercises> q = s.createNamedQuery("Exercises.findAll", Exercises.class); // << TypedQuery
        return q.getResultList();
    }

    @Override
    public void delete(Exercises e) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(e) ? e : s.merge(e));
    }

    @Override
    public List<Exercises> getExercises(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Exercises> cq = cb.createQuery(Exercises.class);
        Root<Exercises> root = cq.from(Exercises.class);

        List<Predicate> predicates = new ArrayList<>();

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + kw.trim().toLowerCase() + "%"));
            // (Tuỳ chọn) mở rộng tìm cả muscleGroup/targetGoal:
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

        // Sắp xếp mặc định: mới nhất trước (fallback theo id nếu thiếu createdAt)
        try {
            cq.orderBy(cb.desc(root.get("createdAt")));
        } catch (Exception ignore) {
            cq.orderBy(cb.desc(root.get("exercisesId")));
        }

        TypedQuery<Exercises> q = s.createQuery(cq); // << TypedQuery

        // page 1-based
        int page = 1;
        if (params != null && params.get("page") != null) {
            try {
                page = Math.max(Integer.parseInt(params.get("page")), 1);
            } catch (NumberFormatException ignore) {
            }
        }
        int start = (page - 1) * PAGE_SIZE;
        q.setFirstResult(start);
        q.setMaxResults(PAGE_SIZE);

        return q.getResultList();
    }

    @Override
    public long countExercises(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Exercises> root = cq.from(Exercises.class);

        List<Predicate> predicates = new ArrayList<>();

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + kw.trim().toLowerCase() + "%"));
            // (Tuỳ chọn) đồng bộ với getExercises nếu mở rộng tìm kiếm:
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

        cq.select(cb.count(root));
        TypedQuery<Long> q = s.createQuery(cq); // << TypedQuery
        return q.getSingleResult();
    }
}
