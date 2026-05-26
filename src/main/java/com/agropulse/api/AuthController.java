package com.agropulse.api;

import com.agropulse.api.dto.RegisterDto;
import com.agropulse.dao.UserRepository;
import com.agropulse.model.User;
import com.agropulse.model.enums.UserRole;
import com.agropulse.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ── POST /auth/login ─────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String email    = (String) body.get("email");
        String password = (String) body.get("password");
        String googleId = (String) body.get("googleId");

        // Google login: has email but no password
        if (googleId != null || (email != null && password == null && username == null)) {
            Optional<User> found = userRepository.findByEmail(email);
            if (found.isPresent()) {
                return ResponseEntity.ok(sanitizeWithToken(found.get()));
            }
            // Auto-create Google user; first user ever becomes ADMIN
            User newUser = new User();
            newUser.setEmail(email);
            String name = (String) body.get("name");
            newUser.setFullName(name != null ? name : email);
            newUser.setUsername(email);
            newUser.setRole(userRepository.existsByRole(UserRole.ADMIN) ? UserRole.OPERATOR : UserRole.ADMIN);
            String avatar = (String) body.get("avatar");
            if (avatar != null) newUser.setAvatar(avatar);
            userRepository.save(newUser);
            return ResponseEntity.ok(sanitizeWithToken(newUser));
        }

        // Standard login by username or email
        Optional<User> userOpt = Optional.empty();
        if (username != null && !username.isBlank()) {
            userOpt = userRepository.findByUsername(username);
        }
        if (userOpt.isEmpty() && email != null && !email.isBlank()) {
            userOpt = userRepository.findByEmail(email);
        }

        // Single generic message for both cases — prevents user enumeration
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }

        User user = userOpt.get();
        if (password != null && user.getPassword() != null) {
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            if (!matches) {
                // Fallback: plain-text comparison for legacy/seed accounts; auto-upgrade to BCrypt
                if (password.equals(user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                } else {
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
                }
            }
        }

        return ResponseEntity.ok(sanitizeWithToken(user));
    }

    // ── POST /auth/register ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto body) {
        if (userRepository.existsByUsername(body.getUsername())) {
            return ResponseEntity.status(409).body(Map.of("error", "El usuario ya existe"));
        }
        User user = new User();
        user.setUsername(body.getUsername());
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user.setFullName(body.getFullName() != null ? body.getFullName() : body.getUsername());
        user.setEmail(body.getEmail());
        user.setRole(userRepository.existsByRole(UserRole.ADMIN) ? UserRole.OPERATOR : UserRole.ADMIN);
        userRepository.save(user);
        return ResponseEntity.ok(sanitizeWithToken(user));
    }

    // ── GET /auth/me ─────────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
    }

    // ── POST /auth/bootstrap-admin ── promotes caller to ADMIN only when no admin exists
    // Safe to leave public: only fires once; afterwards it always returns 409.
    @PostMapping("/bootstrap-admin")
    public ResponseEntity<?> bootstrapAdmin(@RequestBody Map<String, Object> body) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return ResponseEntity.status(409).body(Map.of("error", "Ya existe un administrador"));
        }
        String email = (String) body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email requerido"));
        }
        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        User user = opt.get();
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        return ResponseEntity.ok(sanitizeWithToken(user));
    }

    // ── GET /auth/users ── protected by SecurityConfig (ROLE_ADMIN) ─────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream().map(this::sanitize).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("users", users));
    }

    // ── PUT /auth/users/{id}/role ── protected by SecurityConfig ────────
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable int id,
                                        @RequestBody Map<String, Object> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String roleStr = (String) body.get("role");
        if (roleStr != null) {
            try {
                user.setRole(UserRole.valueOf(roleStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // fallback mapping: "USER" -> OPERATOR
                user.setRole(UserRole.OPERATOR);
            }
        }
        userRepository.save(user);
        return ResponseEntity.ok(sanitize(user));
    }

    // ── POST /auth/sync-google-user ──────────────────────────────────────
    @PostMapping("/sync-google-user")
    public ResponseEntity<?> syncGoogleUser(@RequestBody Map<String, Object> body) {
        String email    = (String) body.get("email");
        String username = (String) body.get("username");
        String fullName = (String) body.get("full_name");
        String avatar   = (String) body.get("avatar");
        String roleStr  = (String) body.get("role");

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email requerido"));
        }

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            return ResponseEntity.ok(sanitize(existing.get()));
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username != null ? username : email);
        user.setFullName(fullName != null ? fullName : email);
        user.setAvatar(avatar);
        UserRole role = UserRole.OPERATOR;
        if (roleStr != null) {
            try { role = UserRole.valueOf(roleStr.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(sanitize(user));
    }

    // ── PUT /auth/users/{id}/greenhouses ── protected by SecurityConfig ──
    @PutMapping("/users/{id}/greenhouses")
    public ResponseEntity<?> setGreenhouses(@PathVariable int id,
                                            @RequestBody Map<String, Object> body) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User user = opt.get();

        Object idsObj = body.get("ids");
        if (idsObj instanceof List) {
            List<Integer> ids = ((List<?>) idsObj).stream()
                    .map(o -> ((Number) o).intValue())
                    .collect(Collectors.toList());
            user.setGreenhouseIds(ids);
        } else {
            user.setGreenhouseIds(new java.util.ArrayList<>());
        }
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ids", user.getGreenhouseIds()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Map<String, Object> sanitizeWithToken(User user) {
        Map<String, Object> map = sanitize(user);
        String role = user.getRole() != null ? user.getRole().name() : "OPERATOR";
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail(), role);
        map.put("token", token);
        return map;
    }

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
