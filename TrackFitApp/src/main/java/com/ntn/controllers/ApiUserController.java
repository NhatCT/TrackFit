package com.ntn.controllers;

import com.ntn.dto.*;
import com.ntn.services.UserService;
import com.ntn.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiUserController {

    @Autowired
    private UserService userDetailsService;

    @PostMapping(path = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @ModelAttribute UserRegistrationDTO info,
                                    @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        if (avatar != null && !avatar.isEmpty()) {
            String contentType = avatar.getContentType();
            if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Chỉ hỗ trợ file JPEG hoặc PNG"));
            }
        }
        UserResponseDTO user = this.userDetailsService.register(info, avatar);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping(path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        if (this.userDetailsService.authenticate(loginDTO.getUsername(), loginDTO.getPassword())) {
            String token = JwtUtils.generateToken(loginDTO.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
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
}