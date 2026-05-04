package com.agropulse.pattern.creational.factory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/** ConcreteCreator: crea sensores de humedad relativa del aire. */
public class HumiditySensorCreator extends SensorCreator {
    @Override
    public Sensor createSensor(String name, String location, int greenhouseId) {
        Sensor s = new Sensor(name, SensorType.HUMIDITY, location, greenhouseId);
        s.setLastValue(60.0); // Valor inicial por defecto en %
        return s;
    }
    @Override public SensorType getSensorType() { return SensorType.HUMIDITY; }
}
