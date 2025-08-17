package com.ntn.dto;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

public class AdminUserFormDTO {

    @NotBlank(message = "Username không được trống")
    private String username;

    private String email;
    private String firstName;
    private String lastName;

    @NotBlank(message = "Role không được trống")
    private String role;

    private String password;

    @Pattern(regexp = "^(Male|Female)$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Giới tính phải là Male hoặc Female")
    private String gender;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate birthDate;

    // NEW: upload & hiển thị
    private MultipartFile avatarFile; // để upload
    private String avatarUrl;         // để hiển thị khi sửa

    // getters/setters ...
    public MultipartFile getAvatarFile() { return avatarFile; }
    public void setAvatarFile(MultipartFile avatarFile) { this.avatarFile = avatarFile; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // các getter/setter khác giữ nguyên

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the birthDate
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * @param birthDate the birthDate to set
     */
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
