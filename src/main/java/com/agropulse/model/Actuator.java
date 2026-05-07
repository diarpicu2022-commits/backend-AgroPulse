package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actuators")
public class Actuator {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false) private String name;
    private String type;       // PUMP, FAN, HEATER, LIGHT, VALVE
    private String status;     // ON, OFF, AUTO
    @Column(name = "greenhouse_id") private int greenhouseId;
    private boolean active;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "gpio_pin")    private Integer gpioPin;
    @Column(name = "active_low")  private boolean activeLow;    // true para relés HW-383
    @Column(name = "device_source") private String deviceSource; // ID del ESP32

    public Actuator() { this.active = true; this.status = "OFF"; this.createdAt = LocalDateTime.now(); }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getName()                     { return name; }
    public void setName(String v)               { this.name = v; }
    public String getType()                     { return type; }
    public void setType(String v)               { this.type = v; }
    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }
    public int getGreenhouseId()                { return greenhouseId; }
    public void setGreenhouseId(int v)          { this.greenhouseId = v; }
    public boolean isActive()                   { return active; }
    public void setActive(boolean v)            { this.active = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }

    public Integer getGpioPin()               { return gpioPin; }
    public void setGpioPin(Integer v)         { this.gpioPin = v; }
    public boolean isActiveLow()              { return activeLow; }
    public void setActiveLow(boolean v)       { this.activeLow = v; }
    public String getDeviceSource()           { return deviceSource; }
    public void setDeviceSource(String v)     { this.deviceSource = v; }

    @Override public String toString() {
        return String.format("Actuator{id=%d, name='%s', type=%s, status=%s}", id, name, type, status);
    }
}
