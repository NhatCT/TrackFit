package com.ntn.repositories.impl;

import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class UserRepositoryImpl extends BaseHibernateRepository implements UserRepository {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public long countAll() {
        var s = currentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public User getUserByUsername(String username) {
        var s = currentSession();
        TypedQuery<User> q = s.createNamedQuery("User.findByUsername", User.class);
        q.setParameter("username", username);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        var s = currentSession();
        TypedQuery<User> q = s.createNamedQuery("User.findByEmail", User.class);
        q.setParameter("email", email);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User addUser(User user) {
        currentSession().persist(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        currentSession().merge(user);
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = this.getUserByUsername(username);
        return user != null && this.passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public List<User> findAll() {
        return currentSession().createNamedQuery("User.findAll", User.class)
                .getResultList();
    }

    @Override
    public User findById(Integer id) {
        return currentSession().get(User.class, id);
    }

    @Override
    public void delete(User user) {
        removeEntity(user);
    }

    @Override
    public List<User> findAllPaged(int page, int pageSize) {
        var s = currentSession();
        return s.createNamedQuery("User.findAll", User.class)
                .setFirstResult(Math.max(0, (page - 1) * pageSize))
                .setMaxResults(pageSize)
                .getResultList();
    }

}
