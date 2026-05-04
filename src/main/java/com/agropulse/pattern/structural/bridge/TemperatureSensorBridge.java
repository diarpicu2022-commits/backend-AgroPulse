package com.agropulse.pattern.structural.bridge;
import com.agropulse.model.enums.SensorType;
/** RefinedAbstraction: sensor de temperatura que funciona con cualquier protocolo. */
public class TemperatureSensorBridge extends AbstractSensorBridge {
    private final String sensorPin;
    public TemperatureSensorBridge(ISensorProtocol protocol, String sensorPin) {
        super(protocol); this.sensorPin = sensorPin;
    }
    @Override public double readCurrentValue(String deviceId) {
        return Math.round(protocol.readValue(deviceId, sensorPin)*10.0)/10.0;
    }
    @Override public SensorType getSensorType() { return SensorType.TEMPERATURE; }
}