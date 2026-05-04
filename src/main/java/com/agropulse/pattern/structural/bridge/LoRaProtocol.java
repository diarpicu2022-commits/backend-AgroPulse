package com.agropulse.pattern.structural.bridge;
public class LoRaProtocol implements ISensorProtocol {
    @Override public double readValue(String d, String p) { return 20.0+Math.random()*8; }
    @Override public boolean sendCommand(String d, String c) { System.out.printf("[LoRa] %s -> %s%n",c,d); return true; }
    @Override public String getProtocolName() { return "LoRa-915MHz"; }
}