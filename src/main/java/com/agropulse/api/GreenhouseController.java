package com.agropulse.api;

import com.agropulse.api.dto.GreenhouseCreateDto;
import com.agropulse.dao.GreenhouseRepository;
import com.agropulse.dao.UserRepository;
import com.agropulse.model.Greenhouse;
import com.agropulse.pattern.structural.facade.IGreenhouseFacade;
import com.agropulse.service.IrrigationService;
import com.agropulse.service.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
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

    @Autowired private GreenhouseRepository greenhouseRepository;
    @Autowired private JdbcTemplate         jdbcTemplate;
    @Autowired private UserRepository       userRepository;
    @Autowired private OwnershipService     ownershipService;

    // ── Patrón FACADE + COLA FIFO ─────────────────────────────────────
    @Autowired private IGreenhouseFacade  greenhouseFacade;   // FACADE
    @Autowired private IrrigationService  irrigationService;  // QUEUE FIFO

    // ── GET /greenhouses ─────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Greenhouse> list = greenhouseRepository.findAll();
        return ResponseEntity.ok(Map.of("greenhouses", list));
    }

    // ── POST /greenhouses ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody GreenhouseCreateDto body) {
        Greenhouse g = new Greenhouse();
        g.setName(body.getName());
        if (body.getLocation()    != null) g.setLocation(body.getLocation());
        if (body.getDescription() != null) g.setDescription(body.getDescription());
        if (body.getOwnerId()     != null) g.setOwnerId(body.getOwnerId());
        if (body.getActive()      != null) g.setActive(body.getActive());
        if (body.getLatitude()    != null) g.setLatitude(body.getLatitude());
        if (body.getLongitude()   != null) g.setLongitude(body.getLongitude());
        if (body.getPhotoUrl()    != null) g.setPhotoUrl(body.getPhotoUrl());
        greenhouseRepository.save(g);
        return ResponseEntity.ok(g);
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

    // ── PATCH /greenhouses/{id}/location ────────────────────────────────
    // Endpoint sin autenticación de usuario: usado exclusivamente por dispositivos IoT
    // que no tienen JWT pero sí conocen el greenhouseId con el que se vincularon.
    @PatchMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable int id,
                                            @RequestBody Map<String, Object> body) {
        Optional<Greenhouse> opt = greenhouseRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Greenhouse g = opt.get();
        if (body.containsKey("latitude"))  g.setLatitude(toDouble(body.get("latitude")));
        if (body.containsKey("longitude")) g.setLongitude(toDouble(body.get("longitude")));
        greenhouseRepository.save(g);
        return ResponseEntity.ok(Map.of("updated", true, "latitude", g.getLatitude(), "longitude", g.getLongitude()));
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
        if (value instanceof Integer i) return i.doubleValue();
        if (value instanceof Long l)    return l.doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (NumberFormatException e) { return null; }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PATRÓN FACADE — GET /greenhouses/{id}/report
    // ══════════════════════════════════════════════════════════════════
    /**
     * Retorna el reporte completo del invernadero mediante la FACADE.
     * Una sola llamada al cliente obtiene: sensores, alertas, cultivo y riego.
     *
     * FACADE: el cliente no accede a ReadingRepository, AlertRepository ni
     * CropDao directamente — la Facade los orquesta de forma transparente.
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<?> getFullReport(@PathVariable int id) {
        return ResponseEntity.ok(greenhouseFacade.getGreenhouseFullReport(id));
    }

    /**
     * Verifica humedad real y activa riego si está por debajo del umbral.
     * FACADE: una llamada dispara lectura de sensor + actuación + log.
     */
    @PostMapping("/{id}/irrigation/trigger")
    public ResponseEntity<?> triggerIrrigation(@PathVariable int id,
                                                @RequestBody Map<String, Object> body) {
        double threshold = body.containsKey("threshold")
                ? toDouble2(body.get("threshold"))
                : 40.0;
        boolean triggered = greenhouseFacade.triggerIrrigationIfNeeded(id, threshold);
        return ResponseEntity.ok(Map.of(
            "patron",     "FACADE — orquesta sensores + actuadores + log",
            "riegoActivo", triggered,
            "umbral",      threshold
        ));
    }

    // ══════════════════════════════════════════════════════════════════
    //  COLA FIFO — /greenhouses/{id}/irrigation/queue
    // ══════════════════════════════════════════════════════════════════

    /**
     * Encola un nuevo horario de riego (FIFO + Builder).
     * ESTRUCTURA: enqueue() — el horario se agrega al final de la cola.
     * PATRÓN: IrrigationService usa internamente Builder + IrrigationQueue.
     */
    @PostMapping("/{id}/irrigation/enqueue")
    public ResponseEntity<?> enqueueIrrigation(@PathVariable int id,
                                                @RequestBody Map<String, Object> body) {
        String zone     = body.getOrDefault("zone",     "ZONE_A").toString();
        int    hour     = toInt2(body.getOrDefault("startHour",   6));
        int    minute   = toInt2(body.getOrDefault("startMinute", 0));
        int    duration = toInt2(body.getOrDefault("durationMinutes", 30));
        double moisture = body.containsKey("targetMoisture")
                          ? toDouble2(body.get("targetMoisture"))
                          : 60.0;

        var schedule = irrigationService.enqueueSchedule(id, zone, hour, minute, duration, moisture);
        return ResponseEntity.ok(Map.of(
            "estructura", "COLA FIFO — enqueue() al final de la cola",
            "patron",     "BUILDER — IrrigationSchedule.Builder construye el horario",
            "encolado",   schedule.getScheduleName(),
            "zona",       schedule.getIrrigationZone(),
            "inicio",     schedule.getStartTime().toString(),
            "duración",   schedule.getDurationMinutes() + " min"
        ));
    }

    /**
     * Estado de la cola FIFO (front + rear) sin extraer elementos.
     * ESTRUCTURA: front() + rear() — consultas no destructivas.
     */
    @GetMapping("/{id}/irrigation/queue")
    public ResponseEntity<?> getQueueStatus(@PathVariable int id) {
        return ResponseEntity.ok(irrigationService.getQueueStatus());
    }

    /**
     * Procesa el próximo horario en la cola (dequeue FIFO).
     * ESTRUCTURA: dequeue() — extrae el primero en entrar.
     */
    @PostMapping("/{id}/irrigation/process-next")
    public ResponseEntity<?> processNextIrrigation(@PathVariable int id) {
        return ResponseEntity.ok(irrigationService.processNext());
    }

    /**
     * Procesa y vacía toda la cola en orden FIFO.
     */
    @PostMapping("/{id}/irrigation/process-all")
    public ResponseEntity<?> processAllIrrigation(@PathVariable int id) {
        return ResponseEntity.ok(irrigationService.processAll());
    }

    // ── Helpers adicionales ───────────────────────────────────────────
    private double toDouble2(Object v) {
        if (v == null)              return 0.0;
        if (v instanceof Number n)  return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0.0; }
    }

    private int toInt2(Object v) {
        if (v == null)              return 0;
        if (v instanceof Number n)  return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}
