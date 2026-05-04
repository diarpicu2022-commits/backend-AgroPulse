package com.agropulse.pattern.creational.factory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/** ConcreteCreator: crea sensores de temperatura configurados para el invernadero. */
public class TemperatureSensorCreator extends SensorCreator {
    @Override
    public Sensor createSensor(String name, String location, int greenhouseId) {
        Sensor s = new Sensor(name, SensorType.TEMPERATURE, location, greenhouseId);
        s.setLastValue(22.0); // Valor inicial por defecto (°C ambiente típico)
        return s;
    }
    @Override public SensorType getSensorType() { return SensorType.TEMPERATURE; }
}
