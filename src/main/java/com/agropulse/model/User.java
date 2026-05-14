package com.agropulse.model;

import com.agropulse.model.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Usuario del sistema. El Proxy protege el acceso a sus datos sensibles. */
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true) private String username;
    private String password;     // Hash BCrypt — no expuesto en JSON
    @Column(name = "full_name")  private String fullName;
    private String email;
    private String phone;
    private String avatar;
    @Enumerated(EnumType.STRING) private UserRole role;
    private boolean active;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "greenhouse_access") private String greenhouseAccess; // JSON array "[1,2,3]"

    public User() { this.active = true; this.createdAt = LocalDateTime.now(); this.role = UserRole.OPERATOR; }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getUsername()                 { return username; }
    public void setUsername(String v)           { this.username = v; }
    public String getPassword()                 { return password; }
    public void setPassword(String v)           { this.password = v; }
    public String getFullName()                 { return fullName; }
    public void setFullName(String v)           { this.fullName = v; }
    public String getEmail()                    { return email; }
    public void setEmail(String v)              { this.email = v; }
    public String getPhone()                    { return phone; }
    public void setPhone(String v)              { this.phone = v; }
    public String getAvatar()                   { return avatar; }
    public void setAvatar(String v)             { this.avatar = v; }
    public UserRole getRole()                   { return role; }
    public void setRole(UserRole v)             { this.role = v; }
    public boolean isActive()                   { return active; }
    public void setActive(boolean v)            { this.active = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public String getGreenhouseAccess()         { return greenhouseAccess; }
    public void setGreenhouseAccess(String v)   { this.greenhouseAccess = v; }

    /** Devuelve los IDs de invernadero asignados (vacío si ninguno). */
    public java.util.List<Integer> getGreenhouseIds() {
        if (greenhouseAccess == null || greenhouseAccess.isBlank()) return new java.util.ArrayList<>();
        try {
            String s = greenhouseAccess.trim().replaceAll("[\\[\\]\\s]", "");
            if (s.isEmpty()) return new java.util.ArrayList<>();
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            for (String part : s.split(",")) { try { ids.add(Integer.parseInt(part.trim())); } catch (NumberFormatException ignored) {} }
            return ids;
        } catch (Exception e) { return new java.util.ArrayList<>(); }
    }

    /** Guarda la lista de IDs como JSON array string. */
    public void setGreenhouseIds(java.util.List<Integer> ids) {
        if (ids == null || ids.isEmpty()) { this.greenhouseAccess = "[]"; return; }
        this.greenhouseAccess = "[" + ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) + "]";
    }

    @Override public String toString() {
        return String.format("User{id=%d, username='%s', role=%s}", id, username, role);
    }
}
