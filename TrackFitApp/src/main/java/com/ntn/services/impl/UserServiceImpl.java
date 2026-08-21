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
import com.ntn.repositories.HealthDataRepository;
import com.ntn.services.UserService;
import com.ntn.services.PremiumService;
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
import org.springframework.transaction.annotation.Transactional;

import com.ntn.dto.CompleteProfileDTO;
import com.ntn.pojo.Goal;
import com.ntn.repositories.GoalRepository;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private PremiumService premiumService;

    @Autowired
    private HealthDataRepository healthRepo;

    @Autowired
    private GoalRepository goalRepo;

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
            user.setAvatarUrl(uploadAvatar(avatar));
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
            
            // Save health data to database
            healthRepo.saveHealthData(healthDataEntity);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật thông tin sức khỏe", e);
        }
    }

    @Override
    public boolean authenticate(String username, String password) {
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
        user.setAvatarUrl(uploadAvatar(avatar));
        user.setUpdatedAt(LocalDateTime.now());
        this.userRepo.updateUser(user);
        return mapToUserResponseDTO(user);
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

    // Max avatar size enforced server-side (independent of any servlet multipart limit).
    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024; // 5MB

    /**
     * Validates the uploaded avatar (size + real image magic number) and uploads it to
     * Cloudinary with an explicit image resource type. Never trusts the client Content-Type.
     *
     * @return the secure URL of the uploaded image
     */
    private String uploadAvatar(MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện vượt quá dung lượng cho phép (tối đa 5MB)");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể tải ảnh đại diện");
        }
        if (bytes.length > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện vượt quá dung lượng cho phép (tối đa 5MB)");
        }
        if (!isSupportedImage(bytes)) {
            throw new IllegalArgumentException("Tệp tải lên không phải ảnh hợp lệ (chỉ chấp nhận JPEG, PNG, GIF, WebP)");
        }
        try {
            Map<String, Object> res = cloudinary.uploader().upload(bytes,
                    ObjectUtils.asMap("resource_type", "image"));
            return res.get("secure_url").toString();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể tải ảnh đại diện");
        }
    }

    /**
     * Server-side magic-number check for the image formats we accept. Prevents a malicious
     * client from smuggling arbitrary content through a spoofed Content-Type.
     */
    private boolean isSupportedImage(byte[] b) {
        if (b == null || b.length < 12) {
            return false;
        }
        // JPEG: FF D8 FF
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((b[0] & 0xFF) == 0x89 && (b[1] & 0xFF) == 0x50 && (b[2] & 0xFF) == 0x4E && (b[3] & 0xFF) == 0x47
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A) {
            return true;
        }
        // GIF: "GIF87a" or "GIF89a"
        if ((b[0] & 0xFF) == 0x47 && (b[1] & 0xFF) == 0x49 && (b[2] & 0xFF) == 0x46 && (b[3] & 0xFF) == 0x38
                && ((b[4] & 0xFF) == 0x37 || (b[4] & 0xFF) == 0x39) && (b[5] & 0xFF) == 0x61) {
            return true;
        }
        // WebP: "RIFF" .... "WEBP"
        if ((b[0] & 0xFF) == 0x52 && (b[1] & 0xFF) == 0x49 && (b[2] & 0xFF) == 0x46 && (b[3] & 0xFF) == 0x46
                && (b[8] & 0xFF) == 0x57 && (b[9] & 0xFF) == 0x45 && (b[10] & 0xFF) == 0x42 && (b[11] & 0xFF) == 0x50) {
            return true;
        }
        return false;
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
        dto.setIsPremium(premiumService.isPremiumActive(user));
        dto.setPremiumExpiresAt(user.getPremiumExpiresAt());
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
            u.setAvatarUrl(uploadAvatar(form.getAvatarFile()));
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
            u.setAvatarUrl(uploadAvatar(form.getAvatarFile()));
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

    @Override
    @Transactional
    public UserResponseDTO registerOrGetGoogleUser(String email, String name, String avatarUrl) {
        User user = this.userRepo.getUserByEmail(email);
        if (user == null) {
            user = new User();
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int count = 1;
            while (this.userRepo.getUserByUsername(username) != null) {
                username = baseUsername + count;
                count++;
            }
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(this.passwordEncoder.encode(UUID.randomUUID().toString()));
            
            String[] parts = name.split(" ", 2);
            if (parts.length > 1) {
                user.setFirstName(parts[0]);
                user.setLastName(parts[1]);
            } else {
                user.setFirstName(name);
                user.setLastName("");
            }
            user.setAvatarUrl(avatarUrl != null ? avatarUrl : "");
            user.setGender("Male"); // default/placeholder gender
            user.setBirthDate(java.time.LocalDate.now().minusYears(20)); // default/placeholder birthdate
            user.setRole("ROLE_USER");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setIsPremium(false);
            
            user = this.userRepo.addUser(user);
        }
        return mapToUserResponseDTO(user);
    }

    @Override
    @Transactional
    public void completeGoogleUserProfile(String username, CompleteProfileDTO dto) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        user.setGender(dto.getGender());
        user.setBirthDate(dto.getBirthDate());
        user.setUpdatedAt(LocalDateTime.now());
        this.userRepo.updateUser(user);

        HealthData hd = new HealthData();
        hd.setUserId(user);
        hd.setHeight(dto.getHeight() != null ? new java.math.BigDecimal(dto.getHeight()) : null);
        hd.setWeight(dto.getWeight() != null ? new java.math.BigDecimal(dto.getWeight()) : null);
        hd.setUpdatedAt(new Date());
        this.healthRepo.saveHealthData(hd);

        Goal g = new Goal();
        g.setUserId(user);
        g.setGoalType(dto.getGoalType() != null && !dto.getGoalType().isBlank() ? dto.getGoalType().trim() : "general_fitness");
        
        String intensity = dto.getIntensity();
        if (intensity != null && !intensity.isBlank()) {
            intensity = intensity.trim().toLowerCase();
            if (intensity.contains("light") || intensity.contains("nhẹ")) {
                intensity = "Light";
            } else if (intensity.contains("hard") || intensity.contains("nặng")) {
                intensity = "Hard";
            } else {
                intensity = "Medium";
            }
        } else {
            intensity = "Medium";
        }
        g.setIntensity(intensity);
        g.setCreatedAt(new Date());
        this.goalRepo.saveGoal(g);
    }
}
