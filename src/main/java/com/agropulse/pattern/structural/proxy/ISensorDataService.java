package com.agropulse.pattern.structural.proxy;

import com.agropulse.model.SensorReading;
import com.agropulse.model.enums.SensorType;
import java.util.List;

public interface ISensorDataService {
    List<SensorReading> getReadings(int greenhouseId, SensorType type, int limit);
    SensorReading getLatestReading(int greenhouseId, SensorType type);
    void recordReading(SensorReading reading);
    double getAverageValue(int greenhouseId, SensorType type, int hours);
}
