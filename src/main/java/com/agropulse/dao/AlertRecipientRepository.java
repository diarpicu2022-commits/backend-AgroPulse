package com.agropulse.dao;

import com.agropulse.model.AlertRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRecipientRepository extends JpaRepository<AlertRecipient, Integer> {
    List<AlertRecipient> findByGreenhouseIdAndActiveTrue(int greenhouseId);
    List<AlertRecipient> findByGreenhouseId(int greenhouseId);
}
