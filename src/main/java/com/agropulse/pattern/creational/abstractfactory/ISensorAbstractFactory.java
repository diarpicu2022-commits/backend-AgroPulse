package com.agropulse.pattern.creational.abstractfactory;

import com.agropulse.model.Sensor;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN ABSTRACT FACTORY — Familias de sensores             ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Proporciona una interfaz para crear familias de objetos     ║
 * ║  relacionados sin especificar sus clases concretas.          ║
 * ║                                                             ║
 * ║  Familias (PDF Abstract Factory):                           ║
 * ║   - IndoorSensorFactory: sensores de interior               ║
 * ║   - OutdoorSensorFactory: sensores de exterior              ║
 * ║   - FieldSensorFactory: sensores de campo abierto           ║
 * ║                                                             ║
 * ║  Productos creados por cada familia:                         ║
 * ║   - TemperatureSensor, HumiditySensor, CO2Sensor            ║
 * ║                                                             ║
 * ║  Justificación en AgroPulse:                                ║
 * ║   Los sensores de interior y exterior tienen diferentes      ║
 * ║   rangos de calibración y precisión. La Abstract Factory    ║
 * ║   garantiza que se use siempre la familia correcta.         ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public interface ISensorAbstractFactory {

    /**
     * Crea un sensor de temperatura adecuado para el ambiente de esta fábrica.
     *
     * @param location     Ubicación física del sensor
     * @param greenhouseId ID del invernadero
     * @return Sensor de temperatura configurado para este ambiente
     */
    Sensor createTemperatureSensor(String location, int greenhouseId);

    /**
     * Crea un sensor de humedad adecuado para el ambiente de esta fábrica.
     */
    Sensor createHumiditySensor(String location, int greenhouseId);

    /**
     * Crea un sensor de CO₂ adecuado para el ambiente de esta fábrica.
     * Solo aplicable a sensores de interior e invernadero.
     */
    Sensor createCO2Sensor(String location, int greenhouseId);

    /**
     * Identifica el tipo de ambiente que esta fábrica cubre.
     */
    String getEnvironmentType();
}
