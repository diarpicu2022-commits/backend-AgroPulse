package com.agropulse.pattern.structural.proxy;

import com.agropulse.model.SensorReading;
import com.agropulse.model.enums.SensorType;
import com.agropulse.model.enums.UserRole;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PATRÓN PROXY — Control de acceso, caché y lazy loading para datos de sensores.
 * Agrega control de roles, caché de 30s, e invalidación automática al escribir.
 */
@Component
public class SensorDataProxy implements ISensorDataService {

    private final RealSensorDataService realService;
    private final Map<String, SensorReading> readingCache    = new ConcurrentHashMap<>();
    private final Map<String, Long>          cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 30_000;
    // ThreadLocal: cada hilo (request) tiene su propio rol sin interferencia entre requests concurrentes
    private final ThreadLocal<UserRole> currentUserRole = ThreadLocal.withInitial(() -> UserRole.OPERATOR);

    public SensorDataProxy(RealSensorDataService realService) { this.realService = realService; }

    private void checkWriteAccess() {
        if (currentUserRole.get() == UserRole.VIEWER)
            throw new SecurityException("[Proxy] Acceso denegado: VIEWER no puede escribir.");
    }

    @Override
    public SensorReading getLatestReading(int greenhouseId, SensorType type) {
        String key = greenhouseId + "-" + type;
        long now   = System.currentTimeMillis();
        if (readingCache.containsKey(key) && now - cacheTimestamps.getOrDefault(key, 0L) < CACHE_TTL_MS) {
            System.out.printf("[Proxy-Cache] HIT: %s%n", key);
            return readingCache.get(key);
        }
        SensorReading result = realService.getLatestReading(greenhouseId, type);
        readingCache.put(key, result);
        cacheTimestamps.put(key, now);
        return result;
    }

    @Override
    public List<SensorReading> getReadings(int greenhouseId, SensorType type, int limit) {
        return realService.getReadings(greenhouseId, type, limit);
    }

    @Override
    public void recordReading(SensorReading reading) {
        checkWriteAccess();
        String key = reading.getGreenhouseId() + "-" + reading.getSensorType();
        readingCache.remove(key); // Invalidar caché
        realService.recordReading(reading);
    }

    @Override
    public double getAverageValue(int greenhouseId, SensorType type, int hours) {
        return realService.getAverageValue(greenhouseId, type, hours);
    }

    public void setCurrentUserRole(UserRole role) { currentUserRole.set(role); }
}
