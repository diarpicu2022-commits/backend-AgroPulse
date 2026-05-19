package com.agropulse.api;

import com.agropulse.dao.ActuatorRepository;
import com.agropulse.model.Actuator;
import com.agropulse.service.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/actuators")
@CrossOrigin(origins = "*")
public class ActuatorController {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private OwnershipService ownershipService;

    // ── GET /actuators?greenhouseId=X ────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer greenhouseId) {
        List<Actuator> actuators = (greenhouseId != null)
            ? actuatorRepository.findByGreenhouseId(greenhouseId)
            : actuatorRepository.findAll();
        return ResponseEntity.ok(Map.of("actuators", actuators));
    }

    // ── POST /actuators ───────────────────────────────────────────────────
    // Upsert: si llega type + gpioPin + greenhouseId y ya existe, actualiza en vez de crear.
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String  deviceSource = (String) body.get("deviceSource");
        String  type         = (String) body.get("type");
        Integer gpioPin      = body.containsKey("gpioPin") ? toInt(body.get("gpioPin")) : null;

        Actuator actuator = null;
        if (type != null && gpioPin != null && gpioPin >= 0 && body.containsKey("greenhouseId")) {
            int ghId = toInt(body.get("greenhouseId"));
            if (ghId > 0) {
                actuator = actuatorRepository
                    .findFirstByTypeAndGpioPinAndGreenhouseId(type, gpioPin, ghId)
                    .orElse(null);
            }
        }
        if (actuator == null) actuator = new Actuator();

        if (body.containsKey("name"))         actuator.setName((String) body.get("name"));
        if (type != null)                     actuator.setType(type);
        if (body.containsKey("status"))       actuator.setStatus((String) body.get("status"));
        if (body.containsKey("greenhouseId")) actuator.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("active"))       actuator.setActive((Boolean) body.get("active"));
        if (gpioPin != null)                  actuator.setGpioPin(gpioPin);
        if (body.containsKey("activeLow"))    actuator.setActiveLow(toBool(body.get("activeLow")));
        if (deviceSource != null)             actuator.setDeviceSource(deviceSource);
        actuatorRepository.save(actuator);
        return ResponseEntity.ok(actuator);
    }

    // ── GET /actuators/{id} ───────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Optional<Actuator> opt = actuatorRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT /actuators/{id} ───────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        if (!ownershipService.canModifyActuator(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para modificar este actuador"));
        Optional<Actuator> opt = actuatorRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Actuator actuator = opt.get();
        if (body.containsKey("name"))         actuator.setName((String) body.get("name"));
        if (body.containsKey("type"))         actuator.setType((String) body.get("type"));
        if (body.containsKey("status"))       actuator.setStatus((String) body.get("status"));
        if (body.containsKey("greenhouseId")) actuator.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("active"))       actuator.setActive((Boolean) body.get("active"));
        if (body.containsKey("gpioPin"))      actuator.setGpioPin(toInt(body.get("gpioPin")));
        if (body.containsKey("activeLow"))    actuator.setActiveLow(toBool(body.get("activeLow")));
        if (body.containsKey("deviceSource")) actuator.setDeviceSource((String) body.get("deviceSource"));
        actuatorRepository.save(actuator);
        return ResponseEntity.ok(actuator);
    }

    // ── DELETE /actuators/{id} ────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id, HttpServletRequest request) {
        if (!ownershipService.canModifyActuator(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar este actuador"));
        if (!actuatorRepository.existsById(id)) return ResponseEntity.notFound().build();
        actuatorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Double) return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private boolean toBool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
