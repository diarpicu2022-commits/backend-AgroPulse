package com.agropulse.pattern.behavioral.observer;
import com.agropulse.model.SensorReading;
import com.agropulse.model.enums.SensorType;
import org.springframework.stereotype.Component;
@Component
public class AlertObserver implements IGreenhouseObserver {
    @Override public void onSensorReading(SensorReading r) {
        if (r.getSensorType()==SensorType.TEMPERATURE && r.getValue()>35)
            System.out.println("[AlertObserver] ALERTA Temperatura alta: "+r.getValue()+"C");
        if (r.getSensorType()==SensorType.HUMIDITY && r.getValue()<30)
            System.out.println("[AlertObserver] ALERTA Humedad critica: "+r.getValue()+"%");
    }
    @Override public String getObserverName() { return "AlertObserver"; }
}