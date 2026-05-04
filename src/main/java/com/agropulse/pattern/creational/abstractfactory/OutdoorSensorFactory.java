package com.agropulse.pattern.creational.abstractfactory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/**
 * ConcreteFactory: crea sensores de EXTERIOR (fuera del invernadero).
 * Los sensores de exterior tienen rangos más amplios por las variaciones
 * climáticas externas (temperatura, viento, lluvia).
 */
public class OutdoorSensorFactory implements ISensorAbstractFactory {

    @Override
    public Sensor createTemperatureSensor(String location, int greenhouseId) {
        Sensor s = new Sensor("Temp-Exterior-" + location, SensorType.TEMPERATURE, location, greenhouseId);
        s.setLastValue(18.0); // Rango exterior: -10 a 45 °C
        return s;
    }

    @Override
    public Sensor createHumiditySensor(String location, int greenhouseId) {
        Sensor s = new Sensor("Hum-Exterior-" + location, SensorType.HUMIDITY, location, greenhouseId);
        s.setLastValue(55.0); // Rango exterior: 20-100 %
        return s;
    }

    @Override
    public Sensor createCO2Sensor(String location, int greenhouseId) {
        // Exterior: sensor de referencia de CO₂ atmosférico
        Sensor s = new Sensor("CO2-Ref-" + location, SensorType.CO2, location, greenhouseId);
        s.setLastValue(415.0); // CO₂ atmosférico estándar: ~415 ppm
        return s;
    }

    @Override public String getEnvironmentType() { return "OUTDOOR"; }
}
