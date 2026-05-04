package com.agropulse.pattern.behavioral.observer;
import com.agropulse.model.SensorReading;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class GreenhouseMonitor implements IGreenhouseSubject {
    private final List<IGreenhouseObserver> observers = new ArrayList<>();
    @Override public void addObserver(IGreenhouseObserver o) { observers.add(o); }
    @Override public void removeObserver(IGreenhouseObserver o) { observers.remove(o); }
    @Override public void notifyObservers(SensorReading r) { observers.forEach(o -> o.onSensorReading(r)); }
    public void processSensorReading(SensorReading r) { System.out.println("[Monitor] "+r); notifyObservers(r); }
}