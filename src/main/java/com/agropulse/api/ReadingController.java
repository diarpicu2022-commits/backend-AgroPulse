package com.agropulse.api;

import com.agropulse.dao.ReadingRepository;
import com.agropulse.model.SensorReading;
import com.agropulse.model.enums.SensorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/readings")
@CrossOrigin(origins = "*")
public class ReadingController {

    @Autowired
    private ReadingRepository readingRepository;

    // ── GET /readings?limit=100&sensor=id&greenhouseId=X ─────────────────
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) Integer sensor,
            @RequestParam(required = false) Integer greenhouseId) {

        PageRequest page = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        List<SensorReading> readings;
        if (sensor != null) {
            readings = readingRepository.findBySensorId(sensor, page);
        } else if (greenhouseId != null) {
            readings = readingRepository.findByGreenhouseIdOrderByTimestampDesc(greenhouseId, page);
        } else {
            readings = readingRepository.findAllByOrderByTimestampDesc(page);
        }
        return ResponseEntity.ok(Map.of("readings", readings));
    }

    // ── POST /readings ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        SensorReading reading = new SensorReading();
        if (body.containsKey("sensorId"))    reading.setSensorId(toInt(body.get("sensorId")));
        if (body.containsKey("value"))       reading.setValue(toDouble(body.get("value")));
        if (body.containsKey("greenhouseId"))reading.setGreenhouseId(toInt(body.get("greenhouseId")));
        if (body.containsKey("source"))      reading.setSource((String) body.get("source"));
        if (body.containsKey("sensorType")) {
            try {
                reading.setSensorType(SensorType.valueOf(((String) body.get("sensorType")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                reading.setSensorType(SensorType.TEMPERATURE);
            }
        }
        readingRepository.save(reading);
        return ResponseEntity.ok(reading);
    }

    // ── Helpers ──────────────────────────────────────────────────────────
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
