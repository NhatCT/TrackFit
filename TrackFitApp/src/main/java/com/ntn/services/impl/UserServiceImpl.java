package com.ntn.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntn.dto.AdminUserFormDTO;
import com.ntn.dto.ChangePasswordDTO;
import com.ntn.dto.HealthDataDTO;
import com.ntn.dto.UserRegistrationDTO;
import com.ntn.dto.UserResponseDTO;
import com.ntn.pojo.HealthData;
import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public UserResponseDTO getUserByUsername(String username) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }
        return mapToUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = this.userRepo.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy email");
        }
        return mapToUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO register(UserRegistrationDTO dto, MultipartFile avatar) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu không khớp");
        }
        if (userRepo.getUserByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại");
        }
        if (userRepo.getUserByEmail(dto.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        user.setBirthDate(dto.getBirthDate());
        user.setRole("ROLE_USER");
        user.setCreatedAt(LocalDateTime.now());

        if (avatar != null && !avatar.isEmpty()) {
            try {
                Map<String, Object> res = cloudinary.uploader().upload(avatar.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));
                user.setAvatarUrl(res.get("secure_url").toString());
            } catch (IOException ex) {
                throw new RuntimeException("Không thể tải ảnh đại diện");
            }
        }

        user = this.userRepo.addUser(user);
        return mapToUserResponseDTO(user);
    }

    @Override
    public boolean updateHealthMetrics(String username, HealthDataDTO healthData) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        try {
            HealthData healthDataEntity = new HealthData();
            healthDataEntity.setUserId(user);
            healthDataEntity.setHeight(healthData.getHeight() != null ? new BigDecimal(healthData.getHeight()) : null);
            healthDataEntity.setWeight(healthData.getWeight() != null ? new BigDecimal(healthData.getWeight()) : null);
            healthDataEntity.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

            user.setGender(healthData.getGender());
            user.setBirthDate(healthData.getBirthDate());
            user.setUpdatedAt(LocalDateTime.now());

            this.userRepo.updateUser(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật thông tin sức khỏe");
        }
    }

    @Override
    public boolean authenticate(String username, String password) {
        // SỬA: Dùng BCrypt để so khớp thay vì repo.authenticate(plain)
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        return this.passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        if (!this.passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        user.setPassword(this.passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        this.userRepo.updateUser(user);
    }

    @Override
    public UserResponseDTO updateAvatar(String username, MultipartFile avatar) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Ảnh đại diện không được để trống");
        }
        try {
            Map<String, Object> res = cloudinary.uploader().upload(avatar.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            user.setAvatarUrl(res.get("secure_url").toString());
            user.setUpdatedAt(LocalDateTime.now());
            this.userRepo.updateUser(user);
            return mapToUserResponseDTO(user);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể tải ảnh đại diện");
        }
    }

    @Override
    public UserResponseDTO getUserWithHealthInfo(String username) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return mapToUserResponseDTO(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender());
        dto.setBirthDate(user.getBirthDate());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    @Override
    public List<UserResponseDTO> listAllUsers() {
        return userRepo.findAll().stream().map(this::mapToUserResponseDTO).toList();
    }

    @Override
    public UserResponseDTO getUserById(Integer id) {
        User u = userRepo.findById(id);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return mapToUserResponseDTO(u);
    }

    @Override
    public UserResponseDTO createUserByAdmin(AdminUserFormDTO form) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username không được trống");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được trống");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được trống");
        }
        if (form.getGender() == null || form.getGender().isBlank()) {
            throw new IllegalArgumentException("Giới tính không được trống");
        }
        if (form.getBirthDate() == null) {
            throw new IllegalArgumentException("Ngày sinh không được trống");
        }

        if (userRepo.getUserByUsername(form.getUsername()) != null) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại");
        }
        if (userRepo.getUserByEmail(form.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User u = new User();
        u.setUsername(form.getUsername());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setEmail(form.getEmail());
        u.setFirstName(form.getFirstName());
        u.setLastName(form.getLastName());
        u.setRole(form.getRole() != null ? form.getRole() : "ROLE_USER");
        u.setGender(form.getGender());
        u.setBirthDate(form.getBirthDate());
        u.setCreatedAt(java.time.LocalDateTime.now());

        if (form.getAvatarFile() != null && !form.getAvatarFile().isEmpty()) {
            try {
                var res = cloudinary.uploader().upload(
                        form.getAvatarFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto")
                );
                u.setAvatarUrl(res.get("secure_url").toString());
            } catch (IOException e) {
                throw new RuntimeException("Không thể tải ảnh đại diện");
            }
        }

        u = userRepo.addUser(u);
        return mapToUserResponseDTO(u);
    }

    @Override
    public UserResponseDTO updateUserByAdmin(Integer id, AdminUserFormDTO form) {
        User u = userRepo.findById(id);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }

        if (form.getEmail() != null && !form.getEmail().isBlank()) {
            u.setEmail(form.getEmail());
        }
        if (form.getFirstName() != null && !form.getFirstName().isBlank()) {
            u.setFirstName(form.getFirstName());
        }
        if (form.getLastName() != null && !form.getLastName().isBlank()) {
            u.setLastName(form.getLastName());
        }
        if (form.getRole() != null && !form.getRole().isBlank()) {
            u.setRole(form.getRole());
        }
        if (form.getGender() != null && !form.getGender().isBlank()) {
            u.setGender(form.getGender());
        }
        if (form.getBirthDate() != null) {
            u.setBirthDate(form.getBirthDate());
        }
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        if (form.getAvatarFile() != null && !form.getAvatarFile().isEmpty()) {
            try {
                var res = cloudinary.uploader().upload(
                        form.getAvatarFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto")
                );
                u.setAvatarUrl(res.get("secure_url").toString());
            } catch (IOException e) {
                throw new RuntimeException("Không thể tải ảnh đại diện");
            }
        }

        u.setUpdatedAt(java.time.LocalDateTime.now());
        userRepo.updateUser(u);
        return mapToUserResponseDTO(u);
    }

    @Override
    public void deleteById(Integer id) {
        User u = userRepo.findById(id);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        userRepo.delete(u);
    }
}
