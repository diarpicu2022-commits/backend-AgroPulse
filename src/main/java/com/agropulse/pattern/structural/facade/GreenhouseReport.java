package com.agropulse.pattern.structural.facade;

import com.agropulse.model.enums.SensorType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** DTO de respuesta del Facade: reúne toda la información del invernadero. */
public class GreenhouseReport {
    private final int greenhouseId;
    private final Map<SensorType, Double> sensorReadings;
    private final List<String> activeAlerts;
    private final String cropStatus;
    private final String nextIrrigationTime;
    private final LocalDateTime generatedAt;

    public GreenhouseReport(int greenhouseId, Map<SensorType, Double> readings,
                            List<String> alerts, String cropStatus, String nextIrrigation) {
        this.greenhouseId      = greenhouseId;
        this.sensorReadings    = readings;
        this.activeAlerts      = alerts;
        this.cropStatus        = cropStatus;
        this.nextIrrigationTime = nextIrrigation;
        this.generatedAt       = LocalDateTime.now();
    }

    public int getGreenhouseId()                    { return greenhouseId; }
    public Map<SensorType, Double> getSensorReadings() { return sensorReadings; }
    public List<String> getActiveAlerts()           { return activeAlerts; }
    public String getCropStatus()                   { return cropStatus; }
    public String getNextIrrigationTime()           { return nextIrrigationTime; }
    public LocalDateTime getGeneratedAt()           { return generatedAt; }

    @Override public String toString() {
        return String.format("GreenhouseReport{id=%d, sensors=%d, alerts=%d, crop='%s'}",
                greenhouseId, sensorReadings.size(), activeAlerts.size(), cropStatus);
    }
}
