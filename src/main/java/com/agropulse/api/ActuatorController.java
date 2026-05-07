package com.agropulse.api;

import com.agropulse.dao.ActuatorRepository;
import com.agropulse.model.Actuator;
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

    // ── GET /actuators?greenhouseId=X ────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer greenhouseId) {
        List<Actuator> actuators = (greenhouseId != null)
            ? actuatorRepository.findByGreenhouseId(greenhouseId)
            : actuatorRepository.findAll();
        return ResponseEntity.ok(Map.of("actuators", actuators));
    }

    // ── POST /actuators ───────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Actuator actuator = new Actuator();
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

    // ── GET /actuators/{id} ───────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Optional<Actuator> opt = actuatorRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT /actuators/{id} ───────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body) {
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
    public ResponseEntity<?> delete(@PathVariable int id) {
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
