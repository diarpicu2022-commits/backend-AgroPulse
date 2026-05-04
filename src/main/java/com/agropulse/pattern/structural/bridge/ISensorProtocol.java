package com.agropulse.pattern.structural.bridge;
public interface ISensorProtocol {
    double readValue(String deviceId, String sensorPin);
    boolean sendCommand(String deviceId, String command);
    String getProtocolName();
}