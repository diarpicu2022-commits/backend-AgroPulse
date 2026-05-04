package com.agropulse.model;

import com.agropulse.model.enums.CropStage;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Modelo de dominio: Cultivo del invernadero.
 *
 * Principios POO aplicados (Bloque 3-4 de Diseño de Software):
 *  - Encapsulamiento: todos los atributos son private, acceso via getters/setters
 *  - Abstracción: solo expone lo relevante al dominio agrícola
 *  - Constructores múltiples: por defecto y completo
 *  - toString() para representación textual
 *
 * Modificadores de acceso (Bloque 9):
 *  - private para atributos de estado interno
 *  - public para la interfaz pública (getters/setters)
 *
 * Implementa Cloneable para soportar el Patrón Prototype.
 */
@Entity
@Table(name = "crops")
public class Crop implements Cloneable {

    // ── Atributos (private — encapsulamiento) ─────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;                   // Nombre del cultivo (ej: "Tomate")

    private String variety;               // Variedad (ej: "Cherry")

    @Column(name = "temp_min")
    private double tempMin;               // Temperatura mínima óptima en °C

    @Column(name = "temp_max")
    private double tempMax;               // Temperatura máxima óptima en °C

    @Column(name = "humidity_min")
    private double humidityMin;           // Humedad mínima óptima en %

    @Column(name = "humidity_max")
    private double humidityMax;           // Humedad máxima óptima en %

    @Column(name = "soil_moisture_min")
    private double soilMoistureMin;       // Humedad de suelo mínima en %

    @Column(name = "soil_moisture_max")
    private double soilMoistureMax;       // Humedad de suelo máxima en %

    @Column(name = "planting_date")
    private LocalDate plantingDate;       // Fecha de siembra

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage")
    private CropStage currentStage;       // Etapa actual del ciclo (usa Patrón State)

    private boolean active;               // ¿Está activo este cultivo?

    // ── Constructor por defecto (Bloque 4) ───────────────────────────
    public Crop() {
        this.active       = true;
        this.plantingDate = LocalDate.now();
        this.currentStage = CropStage.SEEDING;
    }

    // ── Constructor completo ──────────────────────────────────────────
    public Crop(String name, String variety,
                double tempMin, double tempMax,
                double humidityMin, double humidityMax,
                double soilMoistureMin, double soilMoistureMax) {
        this();
        this.name             = name;
        this.variety          = variety;
        this.tempMin          = tempMin;
        this.tempMax          = tempMax;
        this.humidityMin      = humidityMin;
        this.humidityMax      = humidityMax;
        this.soilMoistureMin  = soilMoistureMin;
        this.soilMoistureMax  = soilMoistureMax;
    }

    // ── Patrón Prototype — clonación profunda ────────────────────────
    @Override
    public Crop clone() {
        try {
            return (Crop) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar cultivo", e);
        }
    }

    // ── toString (Bloque 4) ──────────────────────────────────────────
    @Override
    public String toString() {
        return String.format("Crop{id=%d, name='%s', variety='%s', stage=%s}",
                id, name, variety, currentStage);
    }

    // ── Getters y Setters (encapsulamiento — Bloque 9) ───────────────
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getName()                   { return name; }
    public void setName(String name)          { this.name = name; }

    public String getVariety()                { return variety; }
    public void setVariety(String variety)    { this.variety = variety; }

    public double getTempMin()                { return tempMin; }
    public void setTempMin(double v)          { this.tempMin = v; }

    public double getTempMax()                { return tempMax; }
    public void setTempMax(double v)          { this.tempMax = v; }

    public double getHumidityMin()            { return humidityMin; }
    public void setHumidityMin(double v)      { this.humidityMin = v; }

    public double getHumidityMax()            { return humidityMax; }
    public void setHumidityMax(double v)      { this.humidityMax = v; }

    public double getSoilMoistureMin()        { return soilMoistureMin; }
    public void setSoilMoistureMin(double v)  { this.soilMoistureMin = v; }

    public double getSoilMoistureMax()        { return soilMoistureMax; }
    public void setSoilMoistureMax(double v)  { this.soilMoistureMax = v; }

    public LocalDate getPlantingDate()                   { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate)  { this.plantingDate = plantingDate; }

    public CropStage getCurrentStage()                   { return currentStage; }
    public void setCurrentStage(CropStage stage)         { this.currentStage = stage; }

    public boolean isActive()               { return active; }
    public void setActive(boolean active)   { this.active = active; }
}
