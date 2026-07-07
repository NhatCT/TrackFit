package com.ntn.controllers;

import com.ntn.dto.*;
import com.ntn.pojo.Goal;
import com.ntn.pojo.HealthData;
import com.ntn.pojo.User;
import com.ntn.repositories.GoalRepository;
import com.ntn.repositories.HealthDataRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.UserService;
import com.ntn.utils.JwtUtils;
import jakarta.validation.Valid;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiUserController {

    @Autowired
    private UserService userDetailsService;
    @Autowired private UserRepository userRepo;
    @Autowired private HealthDataRepository healthRepo;
    @Autowired private GoalRepository goalRepo;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostMapping(
        path = "/register",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> create(
            @Valid @ModelAttribute UserRegistrationDTO info,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,

            @RequestParam(value = "height", required = false) BigDecimal height,     
            @RequestParam(value = "weight", required = false) BigDecimal weight,     
            @RequestParam(value = "goalType", required = false) String goalType,     
            @RequestParam(value = "intensity", required = false) String intensity   
    ) {
        if (avatar != null && !avatar.isEmpty()) {
            String contentType = avatar.getContentType();
            if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Chỉ hỗ trợ file JPEG hoặc PNG"));
            }
        }
        UserResponseDTO userDto = this.userDetailsService.register(info, avatar);

        User u = userRepo.findById(userDto.getUserId());
        if (u == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Đăng ký thành công nhưng không đọc được user vừa tạo"));
        }

        boolean createdAnything = false;

        if (isPositive(height) || isPositive(weight)) {
            HealthData h = new HealthData();
            h.setUserId(u);
            if (isPositive(height)) h.setHeight(height);
            if (isPositive(weight)) h.setWeight(weight);
            h.setUpdatedAt(new Date());
            healthRepo.saveHealthData(h);
            createdAnything = true;
        }
        if ((goalType != null && !goalType.isBlank()) || (intensity != null && !intensity.isBlank())) {
            Goal g = new Goal();
            g.setUserId(u);
            g.setGoalType(
                goalType != null && !goalType.isBlank() ? goalType.trim() : "general_fitness"
            );
            g.setIntensity(normalizeIntensity(intensity)); 
            g.setCreatedAt(new Date());
            goalRepo.saveGoal(g);
            createdAnything = true;
        }

        // 5) Trả về
        return new ResponseEntity<>(
            Map.of(
                "user", userDto,
                "initHealthOrGoal", createdAnything
            ),
            HttpStatus.CREATED
        );
    }

    private boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private String normalizeIntensity(String v) {
        if (v == null) return "Medium";
        String t = v.trim();
        if (t.equalsIgnoreCase("low")) return "Low";
        if (t.equalsIgnoreCase("high")) return "High";
        return "Medium";
    }

    @PostMapping(
        path = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        if (this.userDetailsService.authenticate(loginDTO.getUsername(), loginDTO.getPassword())) {
            UserResponseDTO user = this.userDetailsService.getUserByUsername(loginDTO.getUsername());
            String rawRole = user.getRole();
            String role = (rawRole != null && rawRole.startsWith("ROLE_")) ? rawRole.substring(5) : rawRole;

            String token = JwtUtils.generateToken(user.getUsername(), List.of(role));

            return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "roles", List.of(role)
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Tên người dùng hoặc mật khẩu không đúng"));
    }

    @GetMapping("/secure/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        return new ResponseEntity<>(this.userDetailsService.getUserByUsername(principal.getName()), HttpStatus.OK);
    }

    @GetMapping("/secure/profile/{username}")
    public ResponseEntity<?> getUsernameProfile(@PathVariable("username") String username, Principal principal) {
        if (!principal.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Không có quyền truy cập hồ sơ này"));
        }
        return new ResponseEntity<>(this.userDetailsService.getUserByUsername(username), HttpStatus.OK);
    }

    @PostMapping(path = "/secure/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO data, Principal principal) {
        this.userDetailsService.changePassword(principal.getName(), data.getOldPassword(), data.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PostMapping(path = "/secure/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changeAvatar(@RequestParam("avatar") MultipartFile avatar, Principal principal) {
        if (avatar == null || avatar.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Ảnh đại diện không được để trống"));
        }
        String contentType = avatar.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Chỉ hỗ trợ file JPEG hoặc PNG"));
        }
        UserResponseDTO user = this.userDetailsService.updateAvatar(principal.getName(), avatar);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping(
        path = "/login/google",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody GoogleLoginDTO googleLoginDTO) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + googleLoginDTO.getCredential();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Token Google không hợp lệ hoặc đã hết hạn"));
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = mapper.readValue(response.body(), Map.class);
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Không thể lấy email từ tài khoản Google này"));
            }
            
            UserResponseDTO userDto = this.userDetailsService.registerOrGetGoogleUser(email, name, picture);
            
            boolean isNewUser = this.healthRepo.findByUserId(userDto.getUserId()).isEmpty();
            
            String rawRole = userDto.getRole();
            String role = (rawRole != null && rawRole.startsWith("ROLE_")) ? rawRole.substring(5) : rawRole;
            String token = JwtUtils.generateToken(userDto.getUsername(), List.of(role));
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", userDto.getUserId(),
                "username", userDto.getUsername(),
                "roles", List.of(role),
                "isNewUser", isNewUser
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi trong quá trình xác thực Google: " + e.getMessage()));
        }
    }

    @PostMapping(path = "/secure/profile/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> completeProfile(@Valid @RequestBody CompleteProfileDTO data, Principal principal) {
        this.userDetailsService.completeGoogleUserProfile(principal.getName(), data);
        return ResponseEntity.ok(Map.of("message", "Hoàn tất hồ sơ sức khỏe thành công"));
    }
}
