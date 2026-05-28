package com.agropulse.api;

import com.agropulse.model.Alert;
import com.agropulse.model.enums.AlertLevel;
import com.agropulse.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  AlertController — delega en AlertService (AlertStack LIFO)     ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  OCP (SOLID): el controlador está abierto para extensión        ║
 * ║  (nuevos endpoints de stack) sin modificar el CRUD original.    ║
 * ║                                                                  ║
 * ║  Nuevos endpoints que exponen la PILA LIFO al cliente:          ║
 * ║    GET  /alerts/stack              — estado de la pila           ║
 * ║    GET  /alerts/stack/top          — peek() sin extraer          ║
 * ║    POST /alerts/stack/drain-critical — pop() todas las CRITICAL  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    // Inyección por constructor (mejor práctica que @Autowired en campo)
    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // ── GET /alerts ───────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Alert> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(Map.of("alerts", alerts));
    }

    // ── POST /alerts ──────────────────────────────────────────────────
    /**
     * Crea alerta, la persiste en BD y la apila en AlertStack (push LIFO).
     */
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
        // ESTRUCTURA: createAlert() persiste en BD + push() a la pila LIFO
        Alert saved = alertService.createAlert(alert);
        return ResponseEntity.ok(saved);
    }

    // ── PUT /alerts/{id}/read ─────────────────────────────────────────
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable int id) {
        return alertService.markRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE /alerts/{id} ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!alertService.existsAlert(id)) return ResponseEntity.notFound().build();
        alertService.deleteAlert(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ══ ENDPOINTS DE LA PILA LIFO (AlertStack) ════════════════════════

    // ── GET /alerts/stack ─────────────────────────────────────────────
    /**
     * Devuelve el estado de la PILA LIFO sin extraer elementos.
     * Muestra: tamaño, cima, si hay alertas críticas pendientes.
     *
     * ESTRUCTURA: visualización de la pila sin operación destructiva.
     */
    @GetMapping("/stack")
    public ResponseEntity<?> getStackStatus() {
        return ResponseEntity.ok(alertService.getStackStatus());
    }

    // ── GET /alerts/stack/top ─────────────────────────────────────────
    /**
     * Operación PEEK: consulta la cima de la pila sin extraerla.
     * Devuelve la alerta MÁS RECIENTE sin modificar el estado de la pila.
     *
     * ESTRUCTURA: peek() LIFO — el elemento de mayor prioridad es el último apilado.
     */
    @GetMapping("/stack/top")
    public ResponseEntity<?> peekTop() {
        return alertService.peekTop()
                .map(top -> ResponseEntity.ok(Map.of(
                    "operacion",  "PEEK — cima de la pila LIFO (sin extraer)",
                    "alerta",     top
                )))
                .orElse(ResponseEntity.ok(Map.of("mensaje", "La pila de alertas está vacía")));
    }

    // ── POST /alerts/stack/drain-critical ─────────────────────────────
    /**
     * Operación POP iterativo: extrae todas las alertas CRITICAL de la cima.
     * Caso de uso real: el sistema de notificaciones consume las CRITICAL
     * en orden LIFO — la más reciente se envía primero.
     *
     * ESTRUCTURA: pop() encadenado hasta que la cima no sea CRITICAL.
     */
    @PostMapping("/stack/drain-critical")
    public ResponseEntity<?> drainCritical() {
        List<Alert> drained = alertService.drainCriticalAlerts();
        return ResponseEntity.ok(Map.of(
            "operacion",    "POP iterativo LIFO — alertas CRITICAL extraídas",
            "estructura",   "PILA — LIFO (último creado = primero atendido)",
            "extraídas",    drained.size(),
            "alertas",      drained
        ));
    }

    // ── Helper ────────────────────────────────────────────────────────
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer i) return i;
        if (value instanceof Long l)    return l.intValue();
        if (value instanceof Double d)  return d.intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
