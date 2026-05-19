package com.agropulse.api;

import com.agropulse.dao.UserRepository;
import com.agropulse.model.User;
import com.agropulse.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── GET /users ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream().map(this::sanitize).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("users", users));
    }

    // ── POST /users ──────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        User user = new User();
        if (body.containsKey("username")) user.setUsername((String) body.get("username"));
        if (body.containsKey("fullName")) user.setFullName((String) body.get("fullName"));
        if (body.containsKey("email"))    user.setEmail((String) body.get("email"));
        if (body.containsKey("phone"))    user.setPhone((String) body.get("phone"));
        if (body.containsKey("avatar"))   user.setAvatar((String) body.get("avatar"));
        if (body.containsKey("password")) {
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
        }
        if (body.containsKey("role")) {
            try {
                user.setRole(UserRole.valueOf(((String) body.get("role")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(UserRole.OPERATOR);
            }
        }
        if (body.containsKey("active")) user.setActive((Boolean) body.get("active"));
        userRepository.save(user);
        return ResponseEntity.ok(sanitize(user));
    }

    // ── PUT /users/{id} ──────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User user = opt.get();
        if (body.containsKey("username")) user.setUsername((String) body.get("username"));
        if (body.containsKey("fullName")) user.setFullName((String) body.get("fullName"));
        if (body.containsKey("email"))    user.setEmail((String) body.get("email"));
        if (body.containsKey("phone"))    user.setPhone((String) body.get("phone"));
        if (body.containsKey("avatar"))   user.setAvatar((String) body.get("avatar"));
        if (body.containsKey("password") && body.get("password") != null) {
            String raw = (String) body.get("password");
            if (!raw.isBlank()) user.setPassword(passwordEncoder.encode(raw));
        }
        if (body.containsKey("role")) {
            try {
                user.setRole(UserRole.valueOf(((String) body.get("role")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(UserRole.OPERATOR);
            }
        }
        if (body.containsKey("active")) user.setActive((Boolean) body.get("active"));
        userRepository.save(user);
        return ResponseEntity.ok(sanitize(user));
    }

    // ── DELETE /users/{id} ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        // Remove junction-table rows so the greenhouse user list stays clean
        try { jdbcTemplate.update("DELETE FROM user_greenhouse WHERE user_id = ?", id); } catch (Exception ignored) {}
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── GET /users/{id}/greenhouses ──────────────────────────────────────
    @GetMapping("/{id}/greenhouses")
    public ResponseEntity<?> getGreenhouses(@PathVariable int id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("ids", opt.get().getGreenhouseIds()));
    }

    // ── Helper ───────────────────────────────────────────────────────────
    private Map<String, Object> sanitize(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",             user.getId());
        map.put("username",       user.getUsername());
        map.put("fullName",       user.getFullName());
        map.put("email",          user.getEmail());
        map.put("phone",          user.getPhone());
        map.put("avatar",         user.getAvatar());
        map.put("role",           user.getRole() != null ? user.getRole().name() : null);
        map.put("active",         user.isActive());
        map.put("createdAt",      user.getCreatedAt());
        map.put("greenhouseIds",  user.getGreenhouseIds());
        return map;
    }
}
