package com.ntn.utils;

import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserLookupService {

    @Autowired
    private UserRepository userRepo;

    public User requireByUsername(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return u;
    }

    public User requireById(Integer userId) {
        User u = userRepo.findById(userId);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return u;
    }
}
