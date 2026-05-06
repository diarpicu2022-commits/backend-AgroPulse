package com.agropulse.dao;

import com.agropulse.model.SystemLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {
    List<SystemLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
