package com.agropulse.model;

import com.agropulse.model.enums.SensorType;
import jakarta.persistence.*;

/**
 * Modelo de dominio: Sensor de un invernadero.
 *
 * Herencia (Bloque 5-6): esta clase puede ser extendida por sensores
 * especializados (IndoorSensor, OutdoorSensor) — usada por Abstract Factory.
 *
 * Polimorfismo (Bloque 7): los sensores concretos sobreescriben el método
 * getFormattedValue() para mostrar la unidad correcta.
 */
@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType type;

    private String location;

    @Column(name = "last_value")
    private double lastValue;

    private boolean active;

    @Column(name = "greenhouse_id")
    private int greenhouseId;

    // ── Constructores ─────────────────────────────────────────────────
    public Sensor() { this.active = true; }

    public Sensor(String name, SensorType type, String location, int greenhouseId) {
        this();
        this.name         = name;
        this.type         = type;
        this.location     = location;
        this.greenhouseId = greenhouseId;
    }

    /**
     * Polimorfismo: devuelve el valor formateado con su unidad.
     * Las subclases pueden sobrescribir este método.
     */
    public String getFormattedValue() {
        return switch (type) {
            case TEMPERATURE   -> String.format("%.1f °C", lastValue);
            case HUMIDITY      -> String.format("%.1f %%", lastValue);
            case SOIL_MOISTURE -> String.format("%.1f %%", lastValue);
            case LIGHT         -> String.format("%.0f lux", lastValue);
            case CO2           -> String.format("%.0f ppm", lastValue);
            case PH            -> String.format("%.2f pH", lastValue);
            case WIND_SPEED    -> String.format("%.1f m/s", lastValue);
        };
    }

    @Override
    public String toString() {
        return String.format("Sensor{id=%d, name='%s', type=%s, value=%s}",
                id, name, type, getFormattedValue());
    }

    // ── Getters y Setters ─────────────────────────────────────────────
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }
    public String getName()                   { return name; }
    public void setName(String name)          { this.name = name; }
    public SensorType getType()               { return type; }
    public void setType(SensorType type)      { this.type = type; }
    public String getLocation()               { return location; }
    public void setLocation(String location)  { this.location = location; }
    public double getLastValue()              { return lastValue; }
    public void setLastValue(double v)        { this.lastValue = v; }
    public boolean isActive()                 { return active; }
    public void setActive(boolean active)     { this.active = active; }
    public int getGreenhouseId()              { return greenhouseId; }
    public void setGreenhouseId(int id)       { this.greenhouseId = id; }
}
