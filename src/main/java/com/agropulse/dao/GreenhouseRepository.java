package com.agropulse.dao;

import com.agropulse.model.Greenhouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenhouseRepository extends JpaRepository<Greenhouse, Integer> {}
