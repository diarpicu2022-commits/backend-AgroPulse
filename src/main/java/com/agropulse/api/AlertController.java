package com.agropulse.api;

import com.agropulse.dao.AlertRepository;
import com.agropulse.model.Alert;
import com.agropulse.model.enums.AlertLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    // ── GET /alerts ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Alert> alerts = alertRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(Map.of("alerts", alerts));
    }

    // ── POST /alerts ──────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Alert alert = new Alert();
        if (body.containsKey("message"))      alert.setMessage((String) body.get("message"));
        if (body.containsKey("type"))         alert.setType((String) body.get("type"));
        if (body.containsKey("title"))        alert.setTitle((String) body.get("title"));
        if (body.containsKey("greenhouseId")) alert.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("sent"))         alert.setSent((Boolean) body.get("sent"));
        if (body.containsKey("level")) {
            try {
                alert.setLevel(AlertLevel.valueOf(((String) body.get("level")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                alert.setLevel(AlertLevel.INFO);
            }
        } else {
            alert.setLevel(AlertLevel.INFO);
        }
        alertRepository.save(alert);
        return ResponseEntity.ok(alert);
    }

    // ── PUT /alerts/{id}/read ─────────────────────────────────────────────
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable int id) {
        Optional<Alert> opt = alertRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Alert alert = opt.get();
        alert.setRead(true);
        alertRepository.save(alert);
        return ResponseEntity.ok(alert);
    }

    // ── DELETE /alerts/{id} ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!alertRepository.existsById(id)) return ResponseEntity.notFound().build();
        alertRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Double) return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
