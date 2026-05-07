package com.agropulse.dao;

import com.agropulse.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {
    List<Sensor> findByGreenhouseId(int greenhouseId);
    List<Sensor> findByActive(boolean active);
    List<Sensor> findByGreenhouseIdAndDeviceSource(int greenhouseId, String deviceSource);
    void deleteByGreenhouseIdAndDeviceSource(int greenhouseId, String deviceSource);
}
