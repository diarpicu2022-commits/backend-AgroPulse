package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_anomalies")
public class SensorAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "sensor_id", nullable = false)
    private int sensorId;

    // Values: OUT_OF_RANGE | NO_DATA | STUCK | SPIKE
    @Column(name = "anomaly_type", nullable = false, length = 20)
    private String anomalyType;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private boolean notified = false;

    @Column(name = "value_at_detection")
    private Double valueAtDetection;

    @Column(columnDefinition = "TEXT")
    private String message;

    public int getId()                             { return id; }
    public void setId(int id)                      { this.id = id; }
    public int getSensorId()                       { return sensorId; }
    public void setSensorId(int v)                 { this.sensorId = v; }
    public String getAnomalyType()                 { return anomalyType; }
    public void setAnomalyType(String v)           { this.anomalyType = v; }
    public LocalDateTime getDetectedAt()           { return detectedAt; }
    public void setDetectedAt(LocalDateTime v)     { this.detectedAt = v; }
    public LocalDateTime getResolvedAt()           { return resolvedAt; }
    public void setResolvedAt(LocalDateTime v)     { this.resolvedAt = v; }
    public boolean isNotified()                    { return notified; }
    public void setNotified(boolean v)             { this.notified = v; }
    public Double getValueAtDetection()            { return valueAtDetection; }
    public void setValueAtDetection(Double v)      { this.valueAtDetection = v; }
    public String getMessage()                     { return message; }
    public void setMessage(String v)               { this.message = v; }
}
