package com.agropulse.model;

import com.agropulse.model.enums.SensorType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Lectura individual de un sensor. Almacenada en la lista doble enlazada SensorReadingHistory. */
@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "sensor_id")  private int sensorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type") private SensorType sensorType;
    private double value;
    private LocalDateTime timestamp;
    private String source;           // MANUAL, ESP32_WIFI, ESP32_LORA, ESP32_SERIAL
    @Column(name = "greenhouse_id") private int greenhouseId;

    public SensorReading() { this.timestamp = LocalDateTime.now(); this.source = "MANUAL"; this.sensorType = SensorType.TEMPERATURE; }
    public SensorReading(int sensorId, SensorType type, double value, int greenhouseId) {
        this(); this.sensorId = sensorId; this.sensorType = type;
        this.value = value; this.greenhouseId = greenhouseId;
    }

    // Getters y Setters
    public int getId()                             { return id; }
    public void setId(int id)                      { this.id = id; }
    public int getSensorId()                       { return sensorId; }
    public void setSensorId(int v)                 { this.sensorId = v; }
    public SensorType getSensorType()              { return sensorType; }
    public void setSensorType(SensorType v)        { this.sensorType = v; }
    public double getValue()                       { return value; }
    public void setValue(double v)                 { this.value = v; }
    public LocalDateTime getTimestamp()            { return timestamp; }
    public void setTimestamp(LocalDateTime v)      { this.timestamp = v; }
    public String getSource()                      { return source; }
    public void setSource(String v)                { this.source = v; }
    public int getGreenhouseId()                   { return greenhouseId; }
    public void setGreenhouseId(int v)             { this.greenhouseId = v; }

    @Override public String toString() {
        return String.format("SensorReading{sensor=%d, type=%s, value=%.2f, ts=%s}",
                sensorId, sensorType, value, timestamp);
    }
}
