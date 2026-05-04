package com.agropulse.pattern.structural.bridge;
public class WiFiProtocol implements ISensorProtocol {
    @Override public double readValue(String d, String p) { return 22.5+Math.random()*5; }
    @Override public boolean sendCommand(String d, String c) { System.out.printf("[WiFi] %s -> %s%n",c,d); return true; }
    @Override public String getProtocolName() { return "WiFi-HTTP"; }
}