package com.agropulse.config;

import com.agropulse.dao.SensorRepository;
import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Corrects sensor type mismatches introduced by the auto-register logic.
 * Runs once at startup; safe to run repeatedly (idempotent).
 *
 * Also ensures the PostgreSQL CHECK constraint on sensors.type includes all
 * current SensorType enum values — ddl-auto=update never alters existing
 * constraints, so new enum values (e.g. HUMIDITY_EXTERNAL) cause constraint
 * violations unless the constraint is rebuilt here first.
 */
@Component
public class SensorTypeFixer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SensorTypeFixer.class);

    private final SensorRepository sensorRepository;
    private final JdbcTemplate jdbc;

    public SensorTypeFixer(SensorRepository sensorRepository, JdbcTemplate jdbc) {
        this.sensorRepository = sensorRepository;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        rebuildTypeConstraint();

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
        else log.info("[SensorFixer] No sensors needed correction.");
    }

    /**
     * Drops the old sensors_type_check constraint (if present) and recreates it
     * with every value currently declared in the SensorType enum.
     * This is necessary because ddl-auto=update never modifies existing constraints.
     */
    private void rebuildTypeConstraint() {
        try {
            jdbc.execute("ALTER TABLE sensors DROP CONSTRAINT IF EXISTS sensors_type_check");

            String values = Arrays.stream(SensorType.values())
                    .map(v -> "'" + v.name() + "'")
                    .collect(Collectors.joining(", "));
            String sql = "ALTER TABLE sensors ADD CONSTRAINT sensors_type_check " +
                         "CHECK (type IN (" + values + "))";
            jdbc.execute(sql);
            log.info("[SensorFixer] Rebuilt sensors_type_check with values: {}", values);
        } catch (Exception e) {
            log.warn("[SensorFixer] Could not rebuild sensors_type_check: {}", e.getMessage());
        }
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
        // DHT11 temperature stored as TEMPERATURE_INTERNAL
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
