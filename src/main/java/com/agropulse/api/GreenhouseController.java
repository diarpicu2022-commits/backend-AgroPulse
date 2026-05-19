package com.agropulse.api;

import com.agropulse.dao.GreenhouseRepository;
import com.agropulse.dao.UserRepository;
import com.agropulse.model.Greenhouse;
import com.agropulse.service.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para invernaderos.
 * Reemplaza el antiguo GreenhouseController que mapeaba a /greenhouse (incorrecto).
 * Ahora mapea a /greenhouses (plural) con CRUD completo.
 */
@RestController
@RequestMapping("/greenhouses")
@CrossOrigin(origins = "*")
public class GreenhouseController {

    @Autowired
    private GreenhouseRepository greenhouseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnershipService ownershipService;

    // ── GET /greenhouses ─────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Greenhouse> list = greenhouseRepository.findAll();
        return ResponseEntity.ok(Map.of("greenhouses", list));
    }

    // ── POST /greenhouses ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Greenhouse g = new Greenhouse();
            if (body.containsKey("name"))        g.setName((String) body.get("name"));
            if (body.containsKey("location"))    g.setLocation((String) body.get("location"));
            if (body.containsKey("description")) g.setDescription((String) body.get("description"));
            if (body.containsKey("ownerId"))     g.setOwnerId(toInt(body.get("ownerId")));
            if (body.containsKey("active"))      g.setActive((Boolean) body.get("active"));
            if (body.containsKey("latitude"))    g.setLatitude(toDouble(body.get("latitude")));
            if (body.containsKey("longitude"))   g.setLongitude(toDouble(body.get("longitude")));
            if (body.containsKey("photoUrl"))    g.setPhotoUrl((String) body.get("photoUrl"));
            greenhouseRepository.save(g);
            return ResponseEntity.ok(g);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() != null) msg += " | " + e.getCause().getMessage();
            return ResponseEntity.status(500).body(Map.of("error", msg));
        }
    }

    // ── GET /greenhouses/{id} ────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Optional<Greenhouse> opt = greenhouseRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT /greenhouses/{id} ────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        if (!ownershipService.canModifyGreenhouse(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para modificar este invernadero"));
        Optional<Greenhouse> opt = greenhouseRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Greenhouse g = opt.get();
        if (body.containsKey("name"))        g.setName((String) body.get("name"));
        if (body.containsKey("location"))    g.setLocation((String) body.get("location"));
        if (body.containsKey("description")) g.setDescription((String) body.get("description"));
        if (body.containsKey("ownerId"))     g.setOwnerId(toInt(body.get("ownerId")));
        if (body.containsKey("active"))      g.setActive((Boolean) body.get("active"));
        if (body.containsKey("latitude"))    g.setLatitude(toDouble(body.get("latitude")));
        if (body.containsKey("longitude"))   g.setLongitude(toDouble(body.get("longitude")));
        if (body.containsKey("photoUrl"))    g.setPhotoUrl((String) body.get("photoUrl"));
        greenhouseRepository.save(g);
        return ResponseEntity.ok(g);
    }

    // ── DELETE /greenhouses/{id} ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id, HttpServletRequest request) {
        if (!ownershipService.canModifyGreenhouse(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar este invernadero"));
        if (!greenhouseRepository.existsById(id)) return ResponseEntity.notFound().build();
        greenhouseRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── GET /greenhouses/{id}/users ──────────────────────────────────────
    @GetMapping("/{id}/users")
    public ResponseEntity<?> getUsersForGreenhouse(@PathVariable int id) {
        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT u.id, u.username, u.full_name, u.email, u.phone, u.avatar, u.role, u.active, u.created_at " +
                "FROM users u JOIN user_greenhouse ug ON u.id = ug.user_id WHERE ug.greenhouse_id = ?", id);
            return ResponseEntity.ok(Map.of("users", users));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("users", List.of()));
        }
    }

    // ── POST /greenhouses/{id}/users ─────────────────────────────────────
    // NO @Transactional: jdbcTemplate (optional junction table) and JPA must run
    // in separate transactions. A shared @Transactional causes PostgreSQL to mark
    // the whole transaction as aborted when the jdbcTemplate INSERT fails, which
    // then breaks the subsequent JPA findById even though the exception was caught.
    @PostMapping("/{id}/users")
    public ResponseEntity<?> assignUser(@PathVariable int id, @RequestBody Map<String, Object> body) {
        int userId = toInt(body.get("userId"));
        try {
            jdbcTemplate.update(
                "INSERT INTO user_greenhouse (user_id, greenhouse_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                userId, id);
        } catch (Exception ignored) {
            // user_greenhouse junction table may not exist in all environments — non-fatal
        }
        // Sync User.greenhouse_access (JPA runs in its own transaction)
        userRepository.findById(userId).ifPresent(user -> {
            java.util.List<Integer> ids = user.getGreenhouseIds();
            if (!ids.contains(id)) {
                ids.add(id);
                user.setGreenhouseIds(ids);
                userRepository.save(user);
            }
        });
        return ResponseEntity.ok(Map.of("assigned", true));
    }

    // ── DELETE /greenhouses/{id}/users/{userId} ──────────────────────────
    // NO @Transactional: same reason as assignUser above.
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<?> removeUser(@PathVariable int id, @PathVariable int userId) {
        try {
            jdbcTemplate.update(
                "DELETE FROM user_greenhouse WHERE greenhouse_id = ? AND user_id = ?",
                id, userId);
        } catch (Exception ignored) {
            // user_greenhouse junction table may not exist in all environments — non-fatal
        }
        // Sync User.greenhouse_access (JPA runs in its own transaction)
        userRepository.findById(userId).ifPresent(user -> {
            java.util.List<Integer> ids = user.getGreenhouseIds();
            if (ids.remove(Integer.valueOf(id))) {
                user.setGreenhouseIds(ids);
                userRepository.save(user);
            }
        });
        return ResponseEntity.ok(Map.of("removed", true));
    }

    // ── Helper ───────────────────────────────────────────────────────────
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Double) return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
