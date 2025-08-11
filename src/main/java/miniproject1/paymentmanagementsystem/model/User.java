package miniproject1.paymentmanagementsystem.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private int roleId;
    private Role role;
    private Integer teamId; // Can be null for admin users
    private Team team;
    private BigDecimal monthlySalary; // Monthly salary for viewers
    private LocalDate salaryEffectiveDate; // When salary starts

    public User() {}

    public User(String name, String email, String passwordHash, int roleId) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.monthlySalary = BigDecimal.ZERO;
    }

    public User(String name, String email, String passwordHash, int roleId, Integer teamId) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.teamId = teamId;
        this.monthlySalary = BigDecimal.ZERO;
    }

    public User(String name, String email, String passwordHash, int roleId, Integer teamId, BigDecimal monthlySalary) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.teamId = teamId;
        this.monthlySalary = monthlySalary != null ? monthlySalary : BigDecimal.ZERO;
        this.salaryEffectiveDate = LocalDate.now();
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary != null ? monthlySalary : BigDecimal.ZERO;
    }

    public LocalDate getSalaryEffectiveDate() { return salaryEffectiveDate; }
    public void setSalaryEffectiveDate(LocalDate salaryEffectiveDate) {
        this.salaryEffectiveDate = salaryEffectiveDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", roleId=" + roleId +
                ", teamId=" + teamId +
                ", monthlySalary=" + monthlySalary +
                '}';
    }
}
