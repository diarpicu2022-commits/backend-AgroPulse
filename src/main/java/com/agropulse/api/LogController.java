package com.agropulse.api;

import com.agropulse.dao.SystemLogRepository;
import com.agropulse.model.SystemLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@CrossOrigin(origins = "*")
public class LogController {

    @Autowired
    private SystemLogRepository logRepository;

    // ── GET /logs?limit=100 ───────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "100") int limit) {
        PageRequest page = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        List<SystemLog> logs = logRepository.findAllByOrderByTimestampDesc(page);
        return ResponseEntity.ok(Map.of("logs", logs));
    }

    // ── POST /logs ────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        SystemLog log = new SystemLog();
        if (body.containsKey("action"))   log.setAction((String) body.get("action"));
        if (body.containsKey("userName")) log.setUserName((String) body.get("userName"));
        if (body.containsKey("details"))  log.setDetails((String) body.get("details"));
        if (body.containsKey("level"))    log.setLevel((String) body.get("level"));
        logRepository.save(log);
        return ResponseEntity.ok(log);
    }
}
