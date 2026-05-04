package com.agropulse.pattern.structural.bridge;
import com.agropulse.model.enums.SensorType;
/**
 * PATRON BRIDGE: separa tipo de sensor (abstraccion) del protocolo (implementacion).
 * Ambas jerarquias pueden crecer independientemente (OCP).
 */
public abstract class AbstractSensorBridge {
    protected final ISensorProtocol protocol;
    protected AbstractSensorBridge(ISensorProtocol protocol) { this.protocol = protocol; }
    public abstract double readCurrentValue(String deviceId);
    public abstract SensorType getSensorType();
    public String getProtocolName() { return protocol.getProtocolName(); }
}