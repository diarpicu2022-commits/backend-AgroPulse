package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_thresholds")
public class SensorThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "sensor_id", nullable = false, unique = true)
    private int sensorId;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "no_data_minutes", nullable = false)
    private int noDataMinutes = 10;

    @Column(name = "stuck_minutes", nullable = false)
    private int stuckMinutes = 30;

    @Column(name = "spike_percent", nullable = false)
    private double spikePercent = 50.0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public int getId()                           { return id; }
    public void setId(int id)                    { this.id = id; }
    public int getSensorId()                     { return sensorId; }
    public void setSensorId(int v)               { this.sensorId = v; }
    public Double getMinValue()                  { return minValue; }
    public void setMinValue(Double v)            { this.minValue = v; }
    public Double getMaxValue()                  { return maxValue; }
    public void setMaxValue(Double v)            { this.maxValue = v; }
    public int getNoDataMinutes()                { return noDataMinutes; }
    public void setNoDataMinutes(int v)          { this.noDataMinutes = v; }
    public int getStuckMinutes()                 { return stuckMinutes; }
    public void setStuckMinutes(int v)           { this.stuckMinutes = v; }
    public double getSpikePercent()              { return spikePercent; }
    public void setSpikePercent(double v)        { this.spikePercent = v; }
    public boolean isActive()                    { return active; }
    public void setActive(boolean v)             { this.active = v; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)    { this.updatedAt = v; }
}
