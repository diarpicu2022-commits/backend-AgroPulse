package com.agropulse.dao;

import com.agropulse.model.Actuator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActuatorRepository extends JpaRepository<Actuator, Integer> {
    List<Actuator> findByGreenhouseId(int greenhouseId);
    List<Actuator> findByGreenhouseIdAndDeviceSource(int greenhouseId, String deviceSource);
    void deleteByGreenhouseIdAndDeviceSource(int greenhouseId, String deviceSource);
    Optional<Actuator> findFirstByTypeAndGpioPinAndGreenhouseId(String type, Integer gpioPin, int greenhouseId);
}
