package com.agropulse.pattern.creational.factory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN FACTORY METHOD — Creación de sensores               ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Delega la responsabilidad de crear sensores a subclases,   ║
 * ║  en lugar de que una clase principal los cree directamente. ║
 * ║                                                             ║
 * ║  Componentes (PDF Factory Method):                          ║
 * ║   - Creator (abstracto): SensorCreator                      ║
 * ║   - ConcreteCreator: TemperatureSensorCreator,              ║
 * ║                      HumiditySensorCreator,                 ║
 * ║                      SoilMoistureSensorCreator              ║
 * ║   - Product (interfaz): Sensor                              ║
 * ║   - ConcreteProduct: objetos Sensor configurados            ║
 * ║                                                             ║
 * ║  Justificación en AgroPulse:                                ║
 * ║   Cada tipo de sensor tiene parámetros de configuración     ║
 * ║   distintos (rangos, unidades, precisión). El Factory       ║
 * ║   Method permite agregar nuevos tipos sin modificar el      ║
 * ║   código existente (Principio Abierto/Cerrado — OCP).       ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public abstract class SensorCreator {

    /**
     * Factory Method — método abstracto que las subclases implementan.
     * Este es el "molde" del patrón Factory Method.
     *
     * @param name         Nombre del sensor
     * @param location     Ubicación física en el invernadero
     * @param greenhouseId ID del invernadero al que pertenece
     * @return Sensor configurado y listo para usar
     */
    public abstract Sensor createSensor(String name, String location, int greenhouseId);

    /**
     * Método de operación de alto nivel que usa el Factory Method.
     * Las subclases no necesitan sobreescribir este método.
     * Aplica el Principio de Responsabilidad Única (SRP).
     */
    public Sensor createAndRegister(String name, String location, int greenhouseId) {
        // 1. Crear el sensor usando el factory method
        Sensor sensor = createSensor(name, location, greenhouseId);

        // 2. Lógica de registro común a todos los tipos de sensor
        System.out.printf("[SensorFactory] Sensor creado: %s%n", sensor);

        return sensor;
    }

    /**
     * Devuelve el tipo de sensor que este Creator genera.
     * Método abstracto — cada subclase indica su tipo.
     */
    public abstract SensorType getSensorType();
}
