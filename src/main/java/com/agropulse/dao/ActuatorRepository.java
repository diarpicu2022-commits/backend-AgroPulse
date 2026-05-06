package com.agropulse.dao;

import com.agropulse.model.Actuator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActuatorRepository extends JpaRepository<Actuator, Integer> {}
