package com.ntn.repositories.impl;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

public abstract class BaseHibernateRepository {

    @Autowired
    protected LocalSessionFactoryBean factory;

    protected Session currentSession() {
        return factory.getObject().getCurrentSession();
    }

    protected <T> T saveOrMerge(T entity, Object id) {
        Session s = currentSession();
        if (id == null) {
            s.persist(entity);
            return entity;
        }
        @SuppressWarnings("unchecked")
        T merged = (T) s.merge(entity);
        return merged;
    }

    protected void removeEntity(Object entity) {
        Session s = currentSession();
        s.remove(s.contains(entity) ? entity : s.merge(entity));
    }
}
