package com.agropulse.dao;

import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {
    List<Sensor> findByGreenhouseId(int greenhouseId);
    List<Sensor> findByActive(boolean active);
    List<Sensor> findByGreenhouseIdAndDeviceSource(int greenhouseId, String deviceSource);
    void deleteByGreenhouseIdAndDeviceSource(int greenhouseId, String deviceSource);
    Optional<Sensor> findFirstByDeviceSourceAndTypeAndGpioPin(String deviceSource, SensorType type, Integer gpioPin);
    Optional<Sensor> findFirstByGreenhouseIdAndType(int greenhouseId, SensorType type);
    Optional<Sensor> findFirstByGreenhouseIdAndDeviceSourceAndType(
        int greenhouseId, String deviceSource, SensorType type);
}
