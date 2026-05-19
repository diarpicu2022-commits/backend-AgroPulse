package com.agropulse.dao;

import com.agropulse.model.SensorAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorAnomalyRepository extends JpaRepository<SensorAnomaly, Integer> {
    Optional<SensorAnomaly> findBySensorIdAndAnomalyTypeAndResolvedAtIsNull(int sensorId, String anomalyType);
    List<SensorAnomaly> findByNotifiedFalse();
    List<SensorAnomaly> findBySensorId(int sensorId);
}
