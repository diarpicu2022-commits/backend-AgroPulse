package com.agropulse.pattern.structural.proxy;

import com.agropulse.model.SensorReading;
import com.agropulse.model.enums.SensorType;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RealSensorDataService implements ISensorDataService {
    @Override
    public List<SensorReading> getReadings(int greenhouseId, SensorType type, int limit) {
        System.out.printf("[RealService] BD: %d lecturas de %s en invernadero #%d%n", limit, type, greenhouseId);
        return new ArrayList<>();
    }
    @Override
    public SensorReading getLatestReading(int greenhouseId, SensorType type) {
        System.out.printf("[RealService] Última lectura de %s en invernadero #%d%n", type, greenhouseId);
        return new SensorReading(1, type, 22.5, greenhouseId);
    }
    @Override
    public void recordReading(SensorReading reading) {
        System.out.printf("[RealService] Guardando: %s%n", reading);
    }
    @Override
    public double getAverageValue(int greenhouseId, SensorType type, int hours) {
        return 22.8;
    }
}
