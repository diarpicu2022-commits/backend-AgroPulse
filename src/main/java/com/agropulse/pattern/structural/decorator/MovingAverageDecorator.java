package com.agropulse.pattern.structural.decorator;
import java.util.LinkedList;
import java.util.Queue;
/**
 * DECORATOR: promedio movil sobre lecturas del sensor.
 * Usa internamente la estructura de datos COLA (FIFO) del PDF de Colas.
 */
public class MovingAverageDecorator extends SensorDecorator {
    private final int windowSize;
    private final Queue<Double> readings;
    public MovingAverageDecorator(ISensorComponent w, int windowSize) {
        super(w); this.windowSize=windowSize; this.readings=new LinkedList<>();
    }
    @Override public double readValue() {
        readings.offer(wrapped.readValue());
        if (readings.size() > windowSize) readings.poll();
        return readings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
    @Override public String getDescription() { return wrapped.getDescription()+" + PromedioMovil("+windowSize+")"; }
}