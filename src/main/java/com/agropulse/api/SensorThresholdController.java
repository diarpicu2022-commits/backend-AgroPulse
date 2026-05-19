package com.agropulse.api;

import com.agropulse.dao.SensorThresholdRepository;
import com.agropulse.model.SensorThreshold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class SensorThresholdController {

    @Autowired
    private SensorThresholdRepository thresholdRepository;

    // GET /sensors/{id}/threshold
    @GetMapping("/sensors/{id}/threshold")
    public ResponseEntity<?> getThreshold(@PathVariable int id) {
        Optional<SensorThreshold> opt = thresholdRepository.findBySensorId(id);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // PUT /sensors/{id}/threshold  (upsert)
    @PutMapping("/sensors/{id}/threshold")
    public ResponseEntity<?> setThreshold(@PathVariable int id,
                                          @RequestBody Map<String, Object> body) {
        SensorThreshold t = thresholdRepository.findBySensorId(id)
                                               .orElse(new SensorThreshold());
        t.setSensorId(id);
        if (body.containsKey("minValue"))      t.setMinValue(toDoubleNullable(body.get("minValue")));
        if (body.containsKey("maxValue"))      t.setMaxValue(toDoubleNullable(body.get("maxValue")));
        if (body.containsKey("noDataMinutes")) t.setNoDataMinutes(toInt(body.get("noDataMinutes")));
        if (body.containsKey("stuckMinutes"))  t.setStuckMinutes(toInt(body.get("stuckMinutes")));
        if (body.containsKey("spikePercent"))  t.setSpikePercent(toDouble(body.get("spikePercent")));
        t.setActive(true);
        t.setUpdatedAt(LocalDateTime.now());
        thresholdRepository.save(t);
        return ResponseEntity.ok(t);
    }

    // DELETE /sensors/{id}/threshold
    @DeleteMapping("/sensors/{id}/threshold")
    public ResponseEntity<?> deleteThreshold(@PathVariable int id) {
        thresholdRepository.findBySensorId(id)
                           .ifPresent(t -> thresholdRepository.delete(t));
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    private int toInt(Object v) {
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long)    return ((Long) v).intValue();
        if (v instanceof Double)  return ((Double) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
    private double toDouble(Object v) {
        if (v instanceof Double)  return (Double) v;
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        if (v instanceof Long)    return ((Long) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0.0; }
    }
    private Double toDoubleNullable(Object v) {
        if (v == null) return null;
        return toDouble(v);
    }
}
