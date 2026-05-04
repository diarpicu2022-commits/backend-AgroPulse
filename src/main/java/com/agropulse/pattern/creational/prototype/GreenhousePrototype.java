package com.agropulse.pattern.creational.prototype;

import com.agropulse.model.Crop;
import com.agropulse.model.Greenhouse;
import com.agropulse.model.Sensor;
import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN PROTOTYPE — Plantilla de invernadero cloneable      ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Permite crear nuevos invernaderos clonando uno existente,  ║
 * ║  en lugar de construirlo desde cero. Ideal cuando la        ║
 * ║  configuración es costosa de recalcular.                    ║
 * ║                                                             ║
 * ║  Ventaja (PDF Prototype):                                   ║
 * ║   "Permite crear nuevas instancias a partir de objetos       ║
 * ║    existentes y luego modificar solo las partes necesarias"  ║
 * ║                                                             ║
 * ║  Justificación en AgroPulse:                                ║
 * ║   Al expandir una finca, el nuevo invernadero generalmente  ║
 * ║   tendrá la misma configuración de cultivos y sensores que  ║
 * ║   el ya existente. Clonar es más rápido que reconfigurar.   ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class GreenhousePrototype implements Cloneable {

    private Greenhouse greenhouse;          // Metadatos del invernadero
    private List<Sensor> sensorTemplate;   // Plantilla de sensores a instalar
    private List<Crop>   cropTemplate;     // Cultivos por defecto de esta finca

    public GreenhousePrototype(Greenhouse greenhouse) {
        this.greenhouse     = greenhouse;
        this.sensorTemplate = new ArrayList<>();
        this.cropTemplate   = new ArrayList<>();
    }

    /** Agrega un sensor a la plantilla (se clonará en cada copia). */
    public void addSensorTemplate(Sensor sensor) {
        sensorTemplate.add(sensor);
    }

    /** Agrega un cultivo a la plantilla. */
    public void addCropTemplate(Crop crop) {
        cropTemplate.add(crop);
    }

    /**
     * Clona el prototipo profundamente.
     * La copia tendrá su propio greenhouse y listas nuevas (no compartidas).
     * El ID del greenhouse nuevo queda en 0 para que la DB asigne uno nuevo.
     */
    @Override
    public GreenhousePrototype clone() {
        try {
            GreenhousePrototype copy = (GreenhousePrototype) super.clone();

            // Clonar el objeto Greenhouse profundamente
            Greenhouse ghCopy = greenhouse.clone();
            ghCopy.setId(0);         // ID 0 = nuevo registro en BD
            ghCopy.setName(greenhouse.getName() + " (Copia)");
            copy.greenhouse = ghCopy;

            // Clonar la lista de sensores (cada sensor como nueva instancia)
            copy.sensorTemplate = new ArrayList<>();
            for (Sensor sensor : sensorTemplate) {
                Sensor sensorCopy = new Sensor(
                    sensor.getName(), sensor.getType(),
                    sensor.getLocation(), 0  // greenhouseId se asignará tras persistir
                );
                copy.sensorTemplate.add(sensorCopy);
            }

            // Clonar la lista de cultivos
            copy.cropTemplate = new ArrayList<>();
            for (Crop crop : cropTemplate) {
                copy.cropTemplate.add(crop.clone());
            }

            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar prototipo de invernadero", e);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────
    public Greenhouse getGreenhouse()     { return greenhouse; }
    public List<Sensor> getSensorTemplate() { return sensorTemplate; }
    public List<Crop>   getCropTemplate()   { return cropTemplate; }

    @Override
    public String toString() {
        return String.format("GreenhousePrototype{name='%s', sensors=%d, crops=%d}",
                greenhouse.getName(), sensorTemplate.size(), cropTemplate.size());
    }
}
