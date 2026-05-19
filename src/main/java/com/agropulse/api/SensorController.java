package com.agropulse.api;

import com.agropulse.dao.SensorRepository;
import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;
import com.agropulse.service.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private OwnershipService ownershipService;

    // ── GET /sensors?greenhouseId=X ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer greenhouseId) {
        List<Sensor> sensors = (greenhouseId != null)
            ? sensorRepository.findByGreenhouseId(greenhouseId)
            : sensorRepository.findAll();
        return ResponseEntity.ok(Map.of("sensors", sensors));
    }

    // ── POST /sensors ────────────────────────────────────────────────────
    // Upsert: si llega deviceSource + type + gpioPin y ya existe, actualiza en vez de crear.
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String deviceSource = (String) body.get("deviceSource");
        SensorType type     = body.containsKey("type") ? parseSensorType((String) body.get("type")) : null;
        Integer gpioPin     = body.containsKey("gpioPin") ? toInt(body.get("gpioPin")) : null;

        Sensor sensor = null;
        if (deviceSource != null && !deviceSource.isBlank() && type != null && gpioPin != null && gpioPin >= 0) {
            sensor = sensorRepository
                .findFirstByDeviceSourceAndTypeAndGpioPin(deviceSource, type, gpioPin)
                .orElse(null);
        }
        if (sensor == null) sensor = new Sensor();

        if (body.containsKey("name"))         sensor.setName((String) body.get("name"));
        if (body.containsKey("location"))     sensor.setLocation((String) body.get("location"));
        if (body.containsKey("greenhouseId")) sensor.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("lastValue"))    sensor.setLastValue(toDouble(body.get("lastValue")));
        if (body.containsKey("active"))       sensor.setActive((Boolean) body.get("active"));
        if (gpioPin != null)                  sensor.setGpioPin(gpioPin);
        if (body.containsKey("protocol"))     sensor.setProtocol((String) body.get("protocol"));
        if (deviceSource != null)             sensor.setDeviceSource(deviceSource);
        if (type != null)                     sensor.setType(type);
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
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        if (!ownershipService.canModifySensor(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para modificar este sensor"));
        Optional<Sensor> opt = sensorRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Sensor sensor = opt.get();
        if (body.containsKey("name"))         sensor.setName((String) body.get("name"));
        if (body.containsKey("location"))     sensor.setLocation((String) body.get("location"));
        if (body.containsKey("greenhouseId")) sensor.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("lastValue"))    sensor.setLastValue(toDouble(body.get("lastValue")));
        if (body.containsKey("active"))       sensor.setActive((Boolean) body.get("active"));
        if (body.containsKey("gpioPin"))      sensor.setGpioPin(toInt(body.get("gpioPin")));
        if (body.containsKey("protocol"))     sensor.setProtocol((String) body.get("protocol"));
        if (body.containsKey("deviceSource")) sensor.setDeviceSource((String) body.get("deviceSource"));
        if (body.containsKey("type"))         sensor.setType(parseSensorType((String) body.get("type")));
        sensorRepository.save(sensor);
        return ResponseEntity.ok(sensor);
    }

    // ── DELETE /sensors/{id} ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id, HttpServletRequest request) {
        if (!ownershipService.canModifySensor(id, request))
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar este sensor"));
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
