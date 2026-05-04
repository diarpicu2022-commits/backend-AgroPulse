package com.agropulse.pattern.creational.factory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/** ConcreteCreator: crea sensores de humedad del suelo. */
public class SoilMoistureSensorCreator extends SensorCreator {
    @Override
    public Sensor createSensor(String name, String location, int greenhouseId) {
        Sensor s = new Sensor(name, SensorType.SOIL_MOISTURE, location, greenhouseId);
        s.setLastValue(45.0); // Valor inicial por defecto en %
        return s;
    }
    @Override public SensorType getSensorType() { return SensorType.SOIL_MOISTURE; }
}
