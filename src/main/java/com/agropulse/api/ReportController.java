package com.agropulse.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    // ── GET /reports/daily-csv ────────────────────────────────────────────
    @GetMapping("/daily-csv")
    public ResponseEntity<?> dailyCsv() {
        return ResponseEntity.ok(Map.of("message", "Reporte CSV no disponible en esta versión"));
    }

    // ── GET /reports/weekly-stats ─────────────────────────────────────────
    @GetMapping("/weekly-stats")
    public ResponseEntity<?> weeklyStats() {
        return ResponseEntity.ok(Map.of(
                "stats", Map.of(),
                "message", "Estadísticas no disponibles"));
    }

    // ── POST /reports/send-email ──────────────────────────────────────────
    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
                "sent", false,
                "message", "Envío de email no configurado"));
    }

    // ── POST /reports/schedule ────────────────────────────────────────────
    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
                "scheduled", false,
                "message", "Programación no disponible"));
    }

    // ── GET /reports/history ──────────────────────────────────────────────
    @GetMapping("/history")
    public ResponseEntity<?> history() {
        return ResponseEntity.ok(Map.of("history", List.of()));
    }
}
