package com.ntn.dto;

/**
 *
 * @author Thanh Nhat
 */

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class UserRegistrationDTO {
    @NotBlank(message = "{user.username.notBlank}")
    @Size(min = 4, max = 50, message = "{user.username.size}")
    private String username;

    @NotBlank(message = "{user.password.notBlank}")
    @Size(min = 6, max = 255, message = "{user.password.size}")
    private String password;

    @NotBlank(message = "{user.confirmPassword.notBlank}")
    private String confirmPassword;

    @NotBlank(message = "{user.email.notBlank}")
    @Email(message = "{user.email.invalid}")
    private String email;

    @NotBlank(message = "{user.firstName.notBlank}")
    @Size(max = 50, message = "{user.firstName.size}")
    private String firstName;

    @NotBlank(message = "{user.lastName.notBlank}")
    @Size(max = 50, message = "{user.lastName.size}")
    private String lastName;

    @NotBlank(message = "{user.gender.notBlank}")
    @Pattern(regexp = "^(Male|Female)$", message = "{user.gender.invalid}")
    private String gender;

    @NotNull(message = "{user.birthDate.notNull}")
    @Past(message = "{user.birthDate.past}")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthDate;

    // Getters và Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}