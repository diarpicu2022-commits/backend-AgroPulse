package com.agropulse.dao;

import com.agropulse.model.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, Integer> {
    Optional<SensorThreshold> findBySensorId(int sensorId);
    List<SensorThreshold> findByActiveTrue();
    void deleteBySensorId(int sensorId);
}
