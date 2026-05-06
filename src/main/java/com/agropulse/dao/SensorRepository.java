package com.agropulse.dao;

import com.agropulse.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {
    List<Sensor> findByGreenhouseId(int greenhouseId);
    List<Sensor> findByActive(boolean active);
}
