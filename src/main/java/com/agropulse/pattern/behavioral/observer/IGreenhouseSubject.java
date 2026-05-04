package com.agropulse.pattern.behavioral.observer;
import com.agropulse.model.SensorReading;
public interface IGreenhouseSubject {
    void addObserver(IGreenhouseObserver o); void removeObserver(IGreenhouseObserver o);
    void notifyObservers(SensorReading r);
}