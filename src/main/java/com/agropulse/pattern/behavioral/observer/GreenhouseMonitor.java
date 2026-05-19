package com.agropulse.pattern.behavioral.observer;

import com.agropulse.model.SensorReading;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PATRÓN OBSERVER — Sujeto que notifica a todos los observadores registrados
 * cuando llega una lectura de sensor. Spring inyecta automáticamente todos los
 * beans que implementan IGreenhouseObserver (ej: AlertObserver).
 */
@Component
public class GreenhouseMonitor implements IGreenhouseSubject {

    private final List<IGreenhouseObserver> observers;

    public GreenhouseMonitor(List<IGreenhouseObserver> observers) {
        this.observers = new CopyOnWriteArrayList<>(observers);
    }

    @Override public void addObserver(IGreenhouseObserver o)    { observers.add(o); }
    @Override public void removeObserver(IGreenhouseObserver o) { observers.remove(o); }
    @Override public void notifyObservers(SensorReading r)      { observers.forEach(o -> o.onSensorReading(r)); }

    public void processSensorReading(SensorReading r) { notifyObservers(r); }
}