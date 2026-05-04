package com.agropulse.pattern.creational.builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN BUILDER — Construcción de horarios de riego         ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Un horario de riego es un objeto complejo con muchas       ║
 * ║  propiedades opcionales: días, horas, duración, zonas, etc. ║
 * ║  El Builder permite construirlo paso a paso, de forma       ║
 * ║  clara y sin constructores con muchos parámetros.           ║
 * ║                                                             ║
 * ║  Ventaja (PDF Builder):                                     ║
 * ║   "Permite construir objetos complejos ajustando            ║
 * ║    configuraciones y propiedades en el camino"              ║
 * ║                                                             ║
 * ║  Justificación en AgroPulse:                                ║
 * ║   Un programa de riego puede tener múltiples variantes:     ║
 * ║   riego diario, semanal, por zona, con sensor de humedad,  ║
 * ║   con duración variable, etc. El Builder encapsula toda     ║
 * ║   esa complejidad de forma legible y flexible.              ║
 * ╚══════════════════════════════════════════════════════════════╝
 */

/** Producto: horario de riego completamente construido. */
public class IrrigationSchedule {

    // ── Atributos (configurados por el Builder) ───────────────────────
    private final String           scheduleName;
    private final int              greenhouseId;
    private final List<DayOfWeek>  activeDays;
    private final LocalTime        startTime;
    private final int              durationMinutes;
    private final double           targetSoilMoisture;  // % objetivo
    private final boolean          autoStopOnRain;
    private final boolean          sensorTriggered;     // Activar solo si humedad < umbral
    private final String           irrigationZone;

    // Constructor privado — solo el Builder puede crear instancias
    private IrrigationSchedule(Builder builder) {
        this.scheduleName        = builder.scheduleName;
        this.greenhouseId        = builder.greenhouseId;
        this.activeDays          = builder.activeDays;
        this.startTime           = builder.startTime;
        this.durationMinutes     = builder.durationMinutes;
        this.targetSoilMoisture  = builder.targetSoilMoisture;
        this.autoStopOnRain      = builder.autoStopOnRain;
        this.sensorTriggered     = builder.sensorTriggered;
        this.irrigationZone      = builder.irrigationZone;
    }

    // ── Builder estático interno ──────────────────────────────────────
    public static class Builder {

        // Atributos obligatorios
        private final String    scheduleName;
        private final int       greenhouseId;

        // Atributos opcionales con valores por defecto
        private List<DayOfWeek> activeDays         = List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
        private LocalTime       startTime          = LocalTime.of(6, 0);  // 6:00 AM por defecto
        private int             durationMinutes    = 30;
        private double          targetSoilMoisture = 60.0;
        private boolean         autoStopOnRain     = true;
        private boolean         sensorTriggered    = false;
        private String          irrigationZone     = "ZONE_ALL";

        /** Constructor del Builder con atributos obligatorios. */
        public Builder(String scheduleName, int greenhouseId) {
            this.scheduleName  = scheduleName;
            this.greenhouseId  = greenhouseId;
        }

        public Builder activeDays(List<DayOfWeek> days) {
            this.activeDays = new ArrayList<>(days);
            return this;
        }

        public Builder startTime(LocalTime time) {
            this.startTime = time;
            return this;
        }

        public Builder durationMinutes(int minutes) {
            this.durationMinutes = minutes;
            return this;
        }

        public Builder targetSoilMoisture(double percentage) {
            this.targetSoilMoisture = percentage;
            return this;
        }

        public Builder autoStopOnRain(boolean stop) {
            this.autoStopOnRain = stop;
            return this;
        }

        public Builder sensorTriggered(boolean triggered) {
            this.sensorTriggered = triggered;
            return this;
        }

        public Builder irrigationZone(String zone) {
            this.irrigationZone = zone;
            return this;
        }

        /** Construye el objeto final — termina el proceso de construcción. */
        public IrrigationSchedule build() {
            return new IrrigationSchedule(this);
        }
    }

    // ── Getters (objeto inmutable — no hay setters) ───────────────────
    public String           getScheduleName()        { return scheduleName; }
    public int              getGreenhouseId()        { return greenhouseId; }
    public List<DayOfWeek>  getActiveDays()          { return activeDays; }
    public LocalTime        getStartTime()           { return startTime; }
    public int              getDurationMinutes()     { return durationMinutes; }
    public double           getTargetSoilMoisture()  { return targetSoilMoisture; }
    public boolean          isAutoStopOnRain()       { return autoStopOnRain; }
    public boolean          isSensorTriggered()      { return sensorTriggered; }
    public String           getIrrigationZone()      { return irrigationZone; }

    @Override
    public String toString() {
        return String.format(
            "IrrigationSchedule{name='%s', zone='%s', days=%s, start=%s, duration=%dm, targetHumidity=%.1f%%}",
            scheduleName, irrigationZone, activeDays, startTime, durationMinutes, targetSoilMoisture
        );
    }
}
