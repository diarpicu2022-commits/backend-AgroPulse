package com.agropulse.dao;

import com.agropulse.model.SensorReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReadingRepository extends JpaRepository<SensorReading, Integer> {
    List<SensorReading> findBySensorId(int sensorId, Pageable pageable);
    List<SensorReading> findAllByOrderByTimestampDesc(Pageable pageable);
    List<SensorReading> findByGreenhouseIdOrderByTimestampDesc(int greenhouseId, Pageable pageable);
}
