package com.agropulse.api;

import com.agropulse.model.SystemSetting;
import com.agropulse.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system-settings")
@CrossOrigin(origins = "*")
public class SystemSettingController {

    @Autowired
    private SystemSettingService service;

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{key}")
    public ResponseEntity<SystemSetting> update(@PathVariable String key,
                                                @RequestBody Map<String, String> body) {
        String value = body.get("value");
        if (value == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(service.upsert(key, value));
    }
}
