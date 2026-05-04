package com.agropulse.pattern.creational.abstractfactory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/**
 * ConcreteFactory: crea sensores de INTERIOR (dentro del invernadero).
 * Los sensores de interior están calibrados para rangos más estrechos y
 * mayor precisión, ya que el ambiente controlado varía menos.
 */
public class IndoorSensorFactory implements ISensorAbstractFactory {

    @Override
    public Sensor createTemperatureSensor(String location, int greenhouseId) {
        Sensor s = new Sensor("Temp-Interior-" + location, SensorType.TEMPERATURE, location, greenhouseId);
        s.setLastValue(24.0); // Rango típico interior: 18-30 °C
        return s;
    }

    @Override
    public Sensor createHumiditySensor(String location, int greenhouseId) {
        Sensor s = new Sensor("Hum-Interior-" + location, SensorType.HUMIDITY, location, greenhouseId);
        s.setLastValue(70.0); // Rango típico interior: 55-85 %
        return s;
    }

    @Override
    public Sensor createCO2Sensor(String location, int greenhouseId) {
        Sensor s = new Sensor("CO2-Interior-" + location, SensorType.CO2, location, greenhouseId);
        s.setLastValue(800.0); // Rango interior: 400-1500 ppm
        return s;
    }

    @Override public String getEnvironmentType() { return "INDOOR"; }
}
