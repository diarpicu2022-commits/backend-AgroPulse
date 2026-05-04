package com.agropulse.pattern.structural.decorator;
public abstract class SensorDecorator implements ISensorComponent {
    protected final ISensorComponent wrapped;
    protected SensorDecorator(ISensorComponent w) { this.wrapped = w; }
    @Override public double readValue() { return wrapped.readValue(); }
    @Override public String getDescription() { return wrapped.getDescription(); }
}