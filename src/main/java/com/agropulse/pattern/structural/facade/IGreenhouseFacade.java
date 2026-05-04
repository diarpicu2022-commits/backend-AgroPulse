package com.agropulse.pattern.structural.facade;

import com.agropulse.model.enums.SensorType;
import com.agropulse.pattern.creational.builder.IrrigationSchedule;
import java.time.LocalTime;

/** Contrato de la fachada del invernadero. Desacopla al cliente de la implementación (ISP). */
public interface IGreenhouseFacade {
    GreenhouseReport getGreenhouseFullReport(int greenhouseId);
    boolean triggerIrrigationIfNeeded(int greenhouseId, double humidityThreshold);
    void recordSensorReadingAndEvaluate(int greenhouseId, int sensorId, SensorType type, double value);
    IrrigationSchedule scheduleWeeklyIrrigation(int greenhouseId, String zoneName,
                                                 LocalTime startTime, int durationMinutes);
}
