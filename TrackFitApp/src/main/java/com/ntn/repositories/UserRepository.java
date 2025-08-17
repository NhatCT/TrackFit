package com.ntn.repositories;


import com.ntn.pojo.User;

public interface UserRepository {

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User addUser(User user);

    void updateUser(User user);

    boolean authenticate(String username, String password);
}