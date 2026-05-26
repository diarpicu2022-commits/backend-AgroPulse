package com.agropulse.api;

import com.agropulse.service.FirmwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/firmware")
@CrossOrigin(origins = "*")
public class FirmwareController {

    @Autowired
    private FirmwareService firmwareService;

    // ── GET /firmware/version — público, rate-limitado por device code ─────────
    @GetMapping("/version")
    public ResponseEntity<?> getVersion(
            @RequestHeader(value = "X-Device-Code", required = false) String deviceCode) {
        try {
            if (deviceCode != null && !deviceCode.isBlank()) {
                if (firmwareService.isRateLimited(deviceCode)) {
                    return ResponseEntity.status(429)
                        .body(Map.of("error", "Demasiadas peticiones. Espera 1 minuto."));
                }
            }
            return ResponseEntity.ok(firmwareService.getVersion());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /firmware/download — requiere código de dispositivo ───────────────
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestHeader(value = "X-Device-Code", required = false) String deviceCode) {
        if (deviceCode == null || deviceCode.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        try {
            byte[] firmware = firmwareService.getFirmwareBytes();
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=firmware.bin")
                .body(firmware);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ── GET /firmware/signature — requiere código de dispositivo ──────────────
    @GetMapping("/signature")
    public ResponseEntity<byte[]> getSignature(
            @RequestHeader(value = "X-Device-Code", required = false) String deviceCode) {
        if (deviceCode == null || deviceCode.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        try {
            byte[] sig = firmwareService.getSignatureBytes();
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(sig);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ── POST /firmware/upload — solo ADMIN (controlado en SecurityConfig) ─────
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("bin") MultipartFile binFile,
            @RequestParam("sig") MultipartFile sigFile,
            @RequestParam("version") String version,
            @RequestParam(value = "minVersionInt", defaultValue = "0") int minVersionInt) {
        if (binFile.isEmpty() || sigFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se requieren bin y sig"));
        }
        try {
            firmwareService.uploadFirmware(binFile, sigFile, version, minVersionInt);
            return ResponseEntity.ok(Map.of(
                "message", "Firmware cargado y verificado correctamente",
                "version",  version
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "Firma inválida: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
