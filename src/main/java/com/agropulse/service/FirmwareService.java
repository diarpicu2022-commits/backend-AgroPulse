package com.agropulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FirmwareService {

    @Value("${agropulse.firmware.dir:./firmware-storage}")
    private String firmwareDir;

    // DER-encoded public key in Base64 — set via FIRMWARE_PUBLIC_KEY env var on Render
    @Value("${agropulse.firmware.public-key:}")
    private String publicKeyB64;

    private final ObjectMapper mapper = new ObjectMapper();

    // Rate limiting: max 1 version check per device per minute
    private final ConcurrentHashMap<String, Long> lastCheckMs = new ConcurrentHashMap<>();
    private static final long RATE_WINDOW_MS = 60_000L;

    // ── /firmware/version ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> getVersion() throws IOException {
        Path meta = Path.of(firmwareDir, "firmware.json");
        if (!Files.exists(meta)) {
            return Map.of("version", "none", "versionInt", 0, "minVersionInt", 0);
        }
        return mapper.readValue(meta.toFile(), Map.class);
    }

    // ── /firmware/download ────────────────────────────────────────────────────

    public byte[] getFirmwareBytes() throws IOException {
        Path bin = Path.of(firmwareDir, "firmware.bin");
        if (!Files.exists(bin)) throw new FileNotFoundException("No firmware binary available");
        return Files.readAllBytes(bin);
    }

    // ── /firmware/signature ───────────────────────────────────────────────────

    public byte[] getSignatureBytes() throws IOException {
        Path sig = Path.of(firmwareDir, "firmware.sig");
        if (!Files.exists(sig)) throw new FileNotFoundException("No firmware signature available");
        return Files.readAllBytes(sig);
    }

    // ── /firmware/upload (ADMIN only) ─────────────────────────────────────────

    public void uploadFirmware(MultipartFile binFile, MultipartFile sigFile,
                                String version, int minVersionInt) throws Exception {
        byte[] binBytes = binFile.getBytes();
        byte[] sigBytes = sigFile.getBytes();

        // Verify ECDSA signature before storing — rejects tampered binaries
        if (publicKeyB64 != null && !publicKeyB64.isBlank()) {
            verifySignature(binBytes, sigBytes);
        }

        // Compute SHA-256 for integrity reference
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(binBytes);
        String sha256 = HexFormat.of().formatHex(hashBytes);

        int versionInt = parseVersionInt(version);

        // Persist files
        Path dir = Path.of(firmwareDir);
        Files.createDirectories(dir);
        Files.write(dir.resolve("firmware.bin"), binBytes);
        Files.write(dir.resolve("firmware.sig"), sigBytes);

        // Persist metadata
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("version",      version);
        meta.put("versionInt",   versionInt);
        meta.put("minVersionInt", minVersionInt);
        meta.put("sha256",       sha256);
        meta.put("fileSize",     binBytes.length);
        meta.put("uploadedAt",   Instant.now().toString());
        mapper.writeValue(dir.resolve("firmware.json").toFile(), meta);
    }

    // ── Rate limiting ─────────────────────────────────────────────────────────

    public boolean isRateLimited(String deviceCode) {
        if (deviceCode == null || deviceCode.isBlank()) return false;
        long now = System.currentTimeMillis();
        Long last = lastCheckMs.get(deviceCode);
        if (last != null && (now - last) < RATE_WINDOW_MS) return true;
        lastCheckMs.put(deviceCode, now);
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void verifySignature(byte[] firmware, byte[] signature) throws Exception {
        // Public key stored as DER base64 (output of: openssl ec -pubout | base64)
        byte[] pubKeyDer = Base64.getMimeDecoder().decode(publicKeyB64);
        KeyFactory kf   = KeyFactory.getInstance("EC");
        PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(pubKeyDer));

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(pubKey);
        sig.update(firmware);
        if (!sig.verify(signature)) {
            throw new SecurityException("Firma ECDSA inválida — binario rechazado");
        }
    }

    private static int parseVersionInt(String version) {
        String[] p = version.trim().split("\\.");
        if (p.length != 3) throw new IllegalArgumentException("Versión debe ser mayor.minor.patch");
        return Integer.parseInt(p[0]) * 10_000
             + Integer.parseInt(p[1]) * 100
             + Integer.parseInt(p[2]);
    }
}
