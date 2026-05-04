package com.agropulse.pattern.creational.abstractfactory;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;

/**
 * ConcreteFactory: crea sensores de CAMPO ABIERTO (cultivos a cielo abierto).
 * Los sensores de campo están optimizados para el suelo: humedad, pH, temperatura.
 * El CO₂ a campo abierto no es crítico, por eso devuelve sensor de pH en su lugar.
 */
public class FieldSensorFactory implements ISensorAbstractFactory {

    @Override
    public Sensor createTemperatureSensor(String location, int greenhouseId) {
        Sensor s = new Sensor("Temp-Campo-" + location, SensorType.TEMPERATURE, location, greenhouseId);
        s.setLastValue(20.0); // Temperatura ambiente campo
        return s;
    }

    @Override
    public Sensor createHumiditySensor(String location, int greenhouseId) {
        // En campo: humedad del suelo más relevante que humedad del aire
        Sensor s = new Sensor("SuMoist-Campo-" + location, SensorType.SOIL_MOISTURE, location, greenhouseId);
        s.setLastValue(40.0); // Humedad suelo campo típico: 30-60 %
        return s;
    }

    @Override
    public Sensor createCO2Sensor(String location, int greenhouseId) {
        // Campo: sensor de pH más relevante que CO₂
        Sensor s = new Sensor("pH-Campo-" + location, SensorType.PH, location, greenhouseId);
        s.setLastValue(6.5); // pH suelo ideal para mayoría de cultivos: 6-7
        return s;
    }

    @Override public String getEnvironmentType() { return "FIELD"; }
}
