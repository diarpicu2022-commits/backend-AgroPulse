package com.agropulse.dao;

import com.agropulse.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Integer> {
    List<Alert> findAllByOrderByCreatedAtDesc();
}
