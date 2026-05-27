package com.agropulse.config;

import com.agropulse.dao.SensorRepository;
import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Corrects sensor type mismatches introduced by the auto-register logic.
 * Runs once at startup; safe to run repeatedly (idempotent).
 *
 * Known bad data: sensors with protocol DHT11 and name containing "Hum"
 * were stored with type TEMPERATURE instead of HUMIDITY_EXTERNAL.
 */
@Component
public class SensorTypeFixer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SensorTypeFixer.class);

    private final SensorRepository sensorRepository;

    public SensorTypeFixer(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @Override
    public void run(String... args) {
        List<Sensor> all = sensorRepository.findAll();
        int fixed = 0;
        for (Sensor s : all) {
            if (shouldFix(s)) {
                SensorType correct = correctType(s);
                log.info("[SensorFixer] id={} name='{}' type {} -> {}", s.getId(), s.getName(), s.getType(), correct);
                s.setType(correct);
                sensorRepository.save(s);
                fixed++;
            }
        }
        if (fixed > 0) log.info("[SensorFixer] Fixed {} sensor(s).", fixed);
    }

    private boolean shouldFix(Sensor s) {
        if (s.getType() == null) return false;
        String name = s.getName() == null ? "" : s.getName().toLowerCase();
        String proto = s.getProtocol() == null ? "" : s.getProtocol().toUpperCase();

        // DHT11 humidity sensor stored as TEMPERATURE
        if ("DHT11".equals(proto) && s.getType() == SensorType.TEMPERATURE
                && (name.contains("hum") || name.contains("humidity"))) {
            return true;
        }
        // DHT22 external temperature stored as TEMPERATURE_INTERNAL (or vice-versa)
        if ("DHT11".equals(proto) && s.getType() == SensorType.TEMPERATURE_INTERNAL) {
            return true;
        }
        return false;
    }

    private SensorType correctType(Sensor s) {
        String name = s.getName() == null ? "" : s.getName().toLowerCase();
        String proto = s.getProtocol() == null ? "" : s.getProtocol().toUpperCase();
        if ("DHT11".equals(proto)) {
            if (name.contains("hum") || name.contains("humidity")) return SensorType.HUMIDITY_EXTERNAL;
            return SensorType.TEMPERATURE_EXTERNAL;
        }
        return s.getType();
    }
}
