package com.agropulse.pattern.structural.decorator;
public class RawSensor implements ISensorComponent {
    private final String name; private final double value;
    public RawSensor(String name, double value) { this.name=name; this.value=value; }
    @Override public double readValue() { return value; }
    @Override public String getDescription() { return "Sensor["+name+"]"; }
}