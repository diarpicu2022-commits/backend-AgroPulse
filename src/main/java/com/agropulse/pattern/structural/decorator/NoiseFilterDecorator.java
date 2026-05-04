package com.agropulse.pattern.structural.decorator;
public class NoiseFilterDecorator extends SensorDecorator {
    public NoiseFilterDecorator(ISensorComponent w) { super(w); }
    @Override public double readValue() {
        double v = wrapped.readValue();
        if (v < -50 || v > 100) { System.out.println("[NoiseFilter] Anomalo: "+v); return wrapped.readValue(); }
        return v;
    }
    @Override public String getDescription() { return wrapped.getDescription()+" + FiltroRuido"; }
}