package com.agropulse.api;

import com.agropulse.dao.RuleRepository;
import com.agropulse.model.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rules")
@CrossOrigin(origins = "*")
public class RuleController {

    @Autowired
    private RuleRepository ruleRepository;

    // ── GET /rules → JSON array (not wrapped) ─────────────────────────────
    @GetMapping
    public ResponseEntity<List<Rule>> getAll() {
        return ResponseEntity.ok(ruleRepository.findAll());
    }

    // ── POST /rules ───────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Rule rule = new Rule();
        if (body.containsKey("name"))           rule.setName((String) body.get("name"));
        if (body.containsKey("sensor"))         rule.setSensor((String) body.get("sensor"));
        if (body.containsKey("condition"))      rule.setCondition((String) body.get("condition"));
        if (body.containsKey("conditionValue")) rule.setConditionValue(toDouble(body.get("conditionValue")));
        if (body.containsKey("action"))         rule.setAction((String) body.get("action"));
        if (body.containsKey("actuatorId"))     rule.setActuatorId(toInt(body.get("actuatorId")));
        if (body.containsKey("active"))         rule.setActive((Boolean) body.get("active"));
        ruleRepository.save(rule);
        return ResponseEntity.ok(rule);
    }

    // ── PUT /rules/{id} ───────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Optional<Rule> opt = ruleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Rule rule = opt.get();
        if (body.containsKey("name"))           rule.setName((String) body.get("name"));
        if (body.containsKey("sensor"))         rule.setSensor((String) body.get("sensor"));
        if (body.containsKey("condition"))      rule.setCondition((String) body.get("condition"));
        if (body.containsKey("conditionValue")) rule.setConditionValue(toDouble(body.get("conditionValue")));
        if (body.containsKey("action"))         rule.setAction((String) body.get("action"));
        if (body.containsKey("actuatorId"))     rule.setActuatorId(toInt(body.get("actuatorId")));
        if (body.containsKey("active"))         rule.setActive((Boolean) body.get("active"));
        ruleRepository.save(rule);
        return ResponseEntity.ok(rule);
    }

    // ── DELETE /rules/{id} ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!ruleRepository.existsById(id)) return ResponseEntity.notFound().build();
        ruleRepository.deleteById(id);
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

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
}
