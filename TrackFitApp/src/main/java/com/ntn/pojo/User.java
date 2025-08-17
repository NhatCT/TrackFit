package com.ntn.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "user")
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
    @NamedQuery(name = "User.findByUserId", query = "SELECT u FROM User u WHERE u.userId = :userId"),
    @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username = :username"),
    @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM User u WHERE u.password = :password"),
    @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
    @NamedQuery(name = "User.findByFirstName", query = "SELECT u FROM User u WHERE u.firstName = :firstName"),
    @NamedQuery(name = "User.findByLastName", query = "SELECT u FROM User u WHERE u.lastName = :lastName"),
    @NamedQuery(name = "User.findByAvatarUrl", query = "SELECT u FROM User u WHERE u.avatarUrl = :avatarUrl"),
    @NamedQuery(name = "User.findByGender", query = "SELECT u FROM User u WHERE u.gender = :gender"),
    @NamedQuery(name = "User.findByBirthDate", query = "SELECT u FROM User u WHERE u.birthDate = :birthDate"),
    @NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role"),
    @NamedQuery(name = "User.findByCreatedAt", query = "SELECT u FROM User u WHERE u.createdAt = :createdAt"),
    @NamedQuery(name = "User.findByUpdatedAt", query = "SELECT u FROM User u WHERE u.updatedAt = :updatedAt")
})
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    private Integer userId;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 4, max = 50, message = "Tên người dùng phải từ 4-50 ký tự")
    @Column(name = "username", nullable = false)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6-255 ký tự")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100)
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50, message = "Họ tối đa 50 ký tự")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên tối đa 50 ký tự")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Size(max = 255)
    @Column(name = "avatar_url")
    private String avatarUrl;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(Male|Female)$", message = "Giới tính phải là Male, Female")
    @Size(max = 6)
    @Column(name = "gender")
    private String gender;

    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 10, message = "Vai trò tối đa 10 ký tự")
    @Column(name = "role")
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Set<Notification> notificationSet;

    @OneToMany(mappedBy = "userId")
    private Set<Statistic> statisticSet;

    @OneToMany(mappedBy = "userId")
    private Set<WorkoutPlan> workoutPlanSet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Set<Goal> goalSet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Set<UserWorkoutHistory> userWorkoutHistorySet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Set<HealthData> healthDataSet;

    public User() {
    }

    public User(Integer userId) {
        this.userId = userId;
    }

    public User(Integer userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters và Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Notification> getNotificationSet() {
        return notificationSet;
    }

    public void setNotificationSet(Set<Notification> notificationSet) {
        this.notificationSet = notificationSet;
    }

    public Set<Statistic> getStatisticSet() {
        return statisticSet;
    }

    public void setStatisticSet(Set<Statistic> statisticSet) {
        this.statisticSet = statisticSet;
    }

    public Set<WorkoutPlan> getWorkoutPlanSet() {
        return workoutPlanSet;
    }

    public void setWorkoutPlanSet(Set<WorkoutPlan> workoutPlanSet) {
        this.workoutPlanSet = workoutPlanSet;
    }

    public Set<Goal> getGoalSet() {
        return goalSet;
    }

    public void setGoalSet(Set<Goal> goalSet) {
        this.goalSet = goalSet;
    }

    public Set<UserWorkoutHistory> getUserWorkoutHistorySet() {
        return userWorkoutHistorySet;
    }

    public void setUserWorkoutHistorySet(Set<UserWorkoutHistory> userWorkoutHistorySet) {
        this.userWorkoutHistorySet = userWorkoutHistorySet;
    }

    public Set<HealthData> getHealthDataSet() {
        return healthDataSet;
    }

    public void setHealthDataSet(Set<HealthData> healthDataSet) {
        this.healthDataSet = healthDataSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.User[ userId=" + userId + " ]";
    }
}