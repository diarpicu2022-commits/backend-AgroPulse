package com.agropulse.pattern.behavioral.observer;
import com.agropulse.model.SensorReading;
public interface IGreenhouseObserver { void onSensorReading(SensorReading r); String getObserverName(); }