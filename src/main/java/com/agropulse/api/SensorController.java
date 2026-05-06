package com.agropulse.api;

import com.agropulse.dao.SensorRepository;
import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/sensors")
@CrossOrigin(origins = "*")
public class SensorController {

    @Autowired
    private SensorRepository sensorRepository;

    // ── GET /sensors ─────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Sensor> sensors = sensorRepository.findAll();
        return ResponseEntity.ok(Map.of("sensors", sensors));
    }

    // ── POST /sensors ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Sensor sensor = new Sensor();
        if (body.containsKey("name"))         sensor.setName((String) body.get("name"));
        if (body.containsKey("location"))     sensor.setLocation((String) body.get("location"));
        if (body.containsKey("greenhouseId")) sensor.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("lastValue"))    sensor.setLastValue(toDouble(body.get("lastValue")));
        if (body.containsKey("active"))       sensor.setActive((Boolean) body.get("active"));
        if (body.containsKey("type")) {
            sensor.setType(parseSensorType((String) body.get("type")));
        }
        sensorRepository.save(sensor);
        return ResponseEntity.ok(sensor);
    }

    // ── GET /sensors/{id} ────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Optional<Sensor> opt = sensorRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT /sensors/{id} ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Optional<Sensor> opt = sensorRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Sensor sensor = opt.get();
        if (body.containsKey("name"))         sensor.setName((String) body.get("name"));
        if (body.containsKey("location"))     sensor.setLocation((String) body.get("location"));
        if (body.containsKey("greenhouseId")) sensor.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("lastValue"))    sensor.setLastValue(toDouble(body.get("lastValue")));
        if (body.containsKey("active"))       sensor.setActive((Boolean) body.get("active"));
        if (body.containsKey("type")) {
            sensor.setType(parseSensorType((String) body.get("type")));
        }
        sensorRepository.save(sensor);
        return ResponseEntity.ok(sensor);
    }

    // ── DELETE /sensors/{id} ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!sensorRepository.existsById(id)) return ResponseEntity.notFound().build();
        sensorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private SensorType parseSensorType(String typeStr) {
        if (typeStr == null) return SensorType.TEMPERATURE;
        try {
            return SensorType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SensorType.TEMPERATURE;
        }
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Double) return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
}
