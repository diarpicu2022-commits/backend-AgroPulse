package com.agropulse.service;

import com.agropulse.dao.*;
import com.agropulse.model.*;
import com.agropulse.model.enums.AlertLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

    @Autowired private SensorThresholdRepository thresholdRepository;
    @Autowired private SensorRepository          sensorRepository;
    @Autowired private SensorAnomalyRepository   anomalyRepository;
    @Autowired private AlertRepository           alertRepository;
    @Autowired private NotificationService       notificationService;
    @Autowired private JdbcTemplate              jdbcTemplate;

    @Scheduled(fixedDelay = 120_000)  // runs every 2 minutes
    public void detect() {
        List<SensorThreshold> thresholds = thresholdRepository.findByActiveTrue();
        for (SensorThreshold threshold : thresholds) {
            try {
                processSensor(threshold);
            } catch (Exception e) {
                log.error("[AnomalyDetection] Error processing sensor "
                        + threshold.getSensorId() + ": " + e.getMessage());
            }
        }
        sendPendingNotifications();
    }

    private void processSensor(SensorThreshold threshold) {
        int sensorId = threshold.getSensorId();
        Optional<Sensor> sensorOpt = sensorRepository.findById(sensorId);
        if (sensorOpt.isEmpty()) return;
        Sensor sensor = sensorOpt.get();

        LocalDateTime now           = LocalDateTime.now();
        LocalDateTime noDataCutoff  = now.minusMinutes(threshold.getNoDataMinutes());
        LocalDateTime stuckCutoff   = now.minusMinutes(threshold.getStuckMinutes());

        // Query last reading timestamp
        LocalDateTime lastTs = null;
        try {
            lastTs = jdbcTemplate.queryForObject(
                "SELECT MAX(timestamp) FROM sensor_readings WHERE sensor_id = ?",
                (rs, rowNum) -> {
                    java.sql.Timestamp ts = rs.getTimestamp(1);
                    return ts != null ? ts.toLocalDateTime() : null;
                }, sensorId);
        } catch (Exception ignored) {}

        // B. NO_DATA
        boolean noData = (lastTs == null || lastTs.isBefore(noDataCutoff));
        checkAnomaly(sensorId, "NO_DATA", noData,
            "Sin datos desde hace más de " + threshold.getNoDataMinutes() + " minutos", null);

        if (!noData) {
            double lastVal = sensor.getLastValue();

            // A. OUT_OF_RANGE
            boolean outOfRange = (threshold.getMinValue() != null && lastVal < threshold.getMinValue())
                              || (threshold.getMaxValue() != null && lastVal > threshold.getMaxValue());
            String orMsg = String.format("Valor %.2f fuera de rango [%s — %s]",
                lastVal,
                threshold.getMinValue() != null ? threshold.getMinValue() : "∞",
                threshold.getMaxValue() != null ? threshold.getMaxValue() : "∞");
            checkAnomaly(sensorId, "OUT_OF_RANGE", outOfRange, orMsg, lastVal);

            // C. STUCK — all readings in last stuckMinutes have spread < 0.01
            List<Double> stuckVals = jdbcTemplate.queryForList(
                "SELECT value FROM sensor_readings WHERE sensor_id = ? AND timestamp > ? " +
                "ORDER BY timestamp DESC LIMIT 20",
                Double.class, sensorId, stuckCutoff);
            boolean stuck = false;
            if (stuckVals.size() >= 3) {
                double max = stuckVals.stream().mapToDouble(Double::doubleValue).max().orElse(0);
                double min = stuckVals.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                stuck = (max - min) < 0.01;
            }
            checkAnomaly(sensorId, "STUCK", stuck,
                "Valor constante durante " + threshold.getStuckMinutes() + " minutos (posible daño en sensor)",
                lastVal);

            // D. SPIKE — change > spikePercent between last two readings
            List<Double> lastTwo = jdbcTemplate.queryForList(
                "SELECT value FROM sensor_readings WHERE sensor_id = ? " +
                "ORDER BY timestamp DESC LIMIT 2",
                Double.class, sensorId);
            boolean spike = false;
            if (lastTwo.size() == 2 && Math.abs(lastTwo.get(1)) > 0.001) {
                double change = Math.abs(lastTwo.get(0) - lastTwo.get(1))
                              / Math.abs(lastTwo.get(1));
                spike = change > (threshold.getSpikePercent() / 100.0);
            }
            String spikeMsg = lastTwo.size() == 2
                ? String.format("Cambio brusco: %.2f → %.2f", lastTwo.get(1), lastTwo.get(0))
                : "Cambio brusco detectado";
            checkAnomaly(sensorId, "SPIKE", spike, spikeMsg,
                lastTwo.isEmpty() ? null : lastTwo.get(0));
        }
    }

    private void checkAnomaly(int sensorId, String type, boolean condition,
                               String message, Double value) {
        Optional<SensorAnomaly> existing = anomalyRepository
                .findBySensorIdAndAnomalyTypeAndResolvedAtIsNull(sensorId, type);
        if (condition) {
            if (existing.isEmpty()) {
                SensorAnomaly a = new SensorAnomaly();
                a.setSensorId(sensorId);
                a.setAnomalyType(type);
                a.setMessage(message);
                a.setValueAtDetection(value);
                a.setNotified(false);
                anomalyRepository.save(a);
            }
        } else {
            existing.ifPresent(a -> {
                a.setResolvedAt(LocalDateTime.now());
                anomalyRepository.save(a);
            });
        }
    }

    private void sendPendingNotifications() {
        List<SensorAnomaly> pending = anomalyRepository.findByNotifiedFalse();
        for (SensorAnomaly anomaly : pending) {
            try {
                Optional<Sensor> sensorOpt = sensorRepository.findById(anomaly.getSensorId());
                if (sensorOpt.isEmpty()) {
                    anomaly.setNotified(true);
                    anomalyRepository.save(anomaly);
                    continue;
                }
                Sensor sensor = sensorOpt.get();
                String ghName = getGreenhouseName(sensor.getGreenhouseId());

                // Insert into alerts table so AlertsPage shows it
                Alert alert = new Alert();
                alert.setLevel("OUT_OF_RANGE".equals(anomaly.getAnomalyType())
                               || "NO_DATA".equals(anomaly.getAnomalyType())
                               ? AlertLevel.CRITICAL : AlertLevel.WARNING);
                alert.setMessage(anomaly.getMessage());
                alert.setTitle("Anomalía: " + anomaly.getAnomalyType()
                               + " — " + sensor.getName());
                alert.setType("SENSOR");
                alert.setGreenhouseId(sensor.getGreenhouseId());
                alertRepository.save(alert);

                // Send email + WhatsApp
                notificationService.notifyAnomaly(
                    anomaly, sensor.getGreenhouseId(), sensor.getName(), ghName);

                anomaly.setNotified(true);
                anomalyRepository.save(anomaly);
            } catch (Exception e) {
                log.error("[AnomalyDetection] Notification failed for anomaly {}: {}",
                        anomaly.getId(), e.getMessage());
            }
        }
    }

    private String getGreenhouseName(int ghId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT name FROM greenhouses WHERE id = ?", String.class, ghId);
        } catch (Exception e) {
            return "Invernadero #" + ghId;
        }
    }
}
