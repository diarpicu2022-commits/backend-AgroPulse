package com.agropulse.dao;

import com.agropulse.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Integer> {
    Optional<SystemSetting> findByKey(String key);
}
