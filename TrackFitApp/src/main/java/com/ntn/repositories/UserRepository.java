package com.ntn.repositories;

import com.ntn.pojo.User;
import java.util.List;

public interface UserRepository {

    long countAll();

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User addUser(User user);

    void updateUser(User user);

    boolean authenticate(String username, String password);


    List<User> findAll();

    User findById(Integer id);

    void delete(User user);

    List<User> findAllPaged(int page, int pageSize);
}
