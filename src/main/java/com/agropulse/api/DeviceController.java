package com.agropulse.api;

import com.agropulse.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el ESP32.
 *
 * POST /api/device/register        — Camino B: ESP32 sube su config
 * GET  /api/device/config/{ghId}   — Camino A+B: ESP32 descarga config al arrancar
 * GET  /api/device/gpios/{ghId}    — Frontend: qué GPIOs están disponibles
 *
 * GPIOs disponibles ESP32 DevKit:
 *   INPUT  (sensores): 4,5,12,13,14,15,16,17,18,19,23,25,26,27,32,33,34,35,36,39
 *   OUTPUT (actuadores): 4,5,12,13,14,15,16,17,18,19,23,25,26,27,32,33
 *   Reservados (sistema): 0,1,2,3,6,7,8,9,10,11,21,22
 */
@RestController
@RequestMapping("/device")
@CrossOrigin(origins = "*")
public class DeviceController {

    private static final List<Integer> GPIO_INPUT  = List.of(
        4,5,12,13,14,15,16,17,18,19,23,25,26,27,32,33,34,35,36,39);
    private static final List<Integer> GPIO_OUTPUT = List.of(
        4,5,12,13,14,15,16,17,18,19,23,25,26,27,32,33);

    @Autowired
    private DeviceService deviceService;

    // ── POST /api/device/register ─────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        int    greenhouseId = toInt(body.get("greenhouseId"));
        String deviceId     = (String) body.getOrDefault("deviceId", "UNKNOWN");

        if (greenhouseId <= 0)
            return ResponseEntity.badRequest().body(Map.of("error", "greenhouseId requerido"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sensors   = (List<Map<String, Object>>) body.getOrDefault("sensors",   List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actuators = (List<Map<String, Object>>) body.getOrDefault("actuators", List.of());

        return ResponseEntity.ok(deviceService.registerDevice(greenhouseId, deviceId, sensors, actuators));
    }

    // ── GET /api/device/config/{greenhouseId} ─────────────────────────────
    @GetMapping("/config/{greenhouseId}")
    public ResponseEntity<?> getConfig(@PathVariable int greenhouseId) {
        return ResponseEntity.ok(deviceService.getDeviceConfig(greenhouseId));
    }

    // ── GET /api/device/gpios/{greenhouseId} ──────────────────────────────
    // Devuelve qué GPIOs están disponibles (no usados aún) para el picker del frontend
    @GetMapping("/gpios/{greenhouseId}")
    public ResponseEntity<?> getAvailableGpios(@PathVariable int greenhouseId) {
        Map<String, Object> config    = deviceService.getDeviceConfig(greenhouseId);
        @SuppressWarnings("unchecked")
        List<Integer> used = (List<Integer>) config.get("usedGpios");

        List<Integer> availInput  = GPIO_INPUT.stream() .filter(g -> !used.contains(g)).toList();
        List<Integer> availOutput = GPIO_OUTPUT.stream().filter(g -> !used.contains(g)).toList();

        return ResponseEntity.ok(Map.of(
            "usedGpios",            used,
            "availableForSensors",  availInput,
            "availableForActuators",availOutput
        ));
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long)    return ((Long) value).intValue();
        if (value instanceof Double)  return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
