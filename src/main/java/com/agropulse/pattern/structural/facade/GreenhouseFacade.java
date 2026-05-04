package com.agropulse.pattern.structural.facade;

import com.agropulse.model.*;
import com.agropulse.model.enums.AlertLevel;
import com.agropulse.model.enums.SensorType;
import com.agropulse.pattern.behavioral.state.CropContext;
import com.agropulse.pattern.creational.builder.IrrigationSchedule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN FACADE — Fachada del invernadero                    ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Proporciona una interfaz simplificada y unificada para     ║
 * ║  interactuar con todos los subsistemas del invernadero:     ║
 * ║  sensores, alertas, cultivos, riego, actuadores, etc.       ║
 * ║                                                             ║
 * ║  Sin Facade, el cliente debe conocer:                       ║
 * ║   - SensorService para leer sensores                        ║
 * ║   - AlertService para generar alertas                       ║
 * ║   - CropContext (State) para gestionar etapas del cultivo   ║
 * ║   - IrrigationSchedule.Builder para programar riegos        ║
 * ║   - etc.                                                    ║
 * ║                                                             ║
 * ║  Con Facade, el cliente llama a un solo método como:        ║
 * ║   facade.getGreenhouseFullReport(id)                        ║
 * ║   facade.triggerIrrigationIfNeeded(id)                      ║
 * ║                                                             ║
 * ║  Analogía del PDF Facade (Sistema de Gestión Médica):       ║
 * ║   Como ClinicaFacade que unifica PacienteService,           ║
 * ║   AgendaService, HistoriaClinica en un solo punto.          ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Component
public class GreenhouseFacade implements IGreenhouseFacade {

    // ── Subsistemas internos (el cliente no los ve directamente) ──────
    // En una implementación Spring Boot completa, estos serían @Autowired
    // Aquí se usan directamente para mantener el ejemplo autocontenido

    // ── Método 1: Reporte completo del invernadero ────────────────────
    /**
     * Consolida en un solo objeto toda la información del invernadero:
     * - Lecturas actuales de todos los sensores
     * - Alertas activas
     * - Estado actual de los cultivos
     * - Próximo horario de riego
     *
     * Invoca mínimo 3 subsistemas internos (cumple requisito del PDF Facade).
     */
    @Override
    public GreenhouseReport getGreenhouseFullReport(int greenhouseId) {
        System.out.printf("[Facade] Generando reporte completo para invernadero #%d%n", greenhouseId);

        // Subsistema 1: lecturas de sensores
        Map<SensorType, Double> readings = readAllSensors(greenhouseId);

        // Subsistema 2: verificar alertas
        List<String> activeAlerts = checkActiveAlerts(greenhouseId);

        // Subsistema 3: estado de cultivos
        String cropStatus = getCropStatus(greenhouseId);

        // Subsistema 4: próximo riego
        String nextIrrigation = getNextIrrigationTime(greenhouseId);

        return new GreenhouseReport(greenhouseId, readings, activeAlerts, cropStatus, nextIrrigation);
    }

    // ── Método 2: Activar riego si la humedad es baja ────────────────
    /**
     * Verifica los sensores de humedad y activa el riego automáticamente
     * si la humedad del suelo está por debajo del umbral configurado.
     * Invoca: SensorSubsystem + ActuatorSubsystem + LoggingSubsystem
     */
    @Override
    public boolean triggerIrrigationIfNeeded(int greenhouseId, double humidityThreshold) {
        System.out.printf("[Facade] Verificando necesidad de riego en invernadero #%d%n", greenhouseId);

        // Subsistema 1: leer humedad del suelo
        double soilMoisture = readSingleSensor(greenhouseId, SensorType.SOIL_MOISTURE);

        if (soilMoisture < humidityThreshold) {
            // Subsistema 2: activar actuador de riego
            activateIrrigationActuator(greenhouseId);
            // Subsistema 3: registrar en log
            logAction(greenhouseId, "Riego automático activado. Humedad suelo: " + soilMoisture + "%");
            return true;
        }

        return false;
    }

    // ── Método 3: Registrar nueva lectura de sensor y evaluar alertas ─
    /**
     * Registra una lectura, la compara con los rangos del cultivo activo,
     * y genera una alerta si está fuera de rango. Todo en una sola llamada.
     */
    @Override
    public void recordSensorReadingAndEvaluate(int greenhouseId, int sensorId,
                                                SensorType type, double value) {
        System.out.printf("[Facade] Registrando lectura: %s = %.2f en invernadero #%d%n",
                type, value, greenhouseId);

        // Subsistema 1: persistir la lectura
        persistReading(sensorId, type, value, greenhouseId);

        // Subsistema 2: evaluar si genera alerta
        evaluateAndGenerateAlert(greenhouseId, type, value);
    }

    // ── Método 4: Programar riego semanal usando el Builder ──────────
    /**
     * Usa internamente el Patrón Builder para crear un horario de riego,
     * luego lo guarda. El cliente no necesita conocer el Builder.
     */
    @Override
    public IrrigationSchedule scheduleWeeklyIrrigation(int greenhouseId, String zoneName,
                                                         LocalTime startTime, int durationMinutes) {
        IrrigationSchedule schedule = new IrrigationSchedule.Builder(
                "Riego Semanal - " + zoneName, greenhouseId)
                .activeDays(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .startTime(startTime)
                .durationMinutes(durationMinutes)
                .targetSoilMoisture(65.0)
                .autoStopOnRain(true)
                .sensorTriggered(true)
                .irrigationZone(zoneName)
                .build();

        System.out.printf("[Facade] Horario creado: %s%n", schedule);
        return schedule;
    }

    // ── Submétodos privados (simulan los subsistemas internos) ────────

    private Map<SensorType, Double> readAllSensors(int greenhouseId) {
        // En producción: consulta la BD vía SensorDao
        return Map.of(
            SensorType.TEMPERATURE,   23.5,
            SensorType.HUMIDITY,      68.0,
            SensorType.SOIL_MOISTURE, 55.0,
            SensorType.CO2,           850.0
        );
    }

    private double readSingleSensor(int greenhouseId, SensorType type) {
        // En producción: consulta el último valor del sensor en BD
        return 45.0; // Valor simulado de humedad del suelo
    }

    private List<String> checkActiveAlerts(int greenhouseId) {
        // En producción: consulta AlertDao
        return List.of();
    }

    private String getCropStatus(int greenhouseId) {
        // En producción: consulta CropDao y CropContext
        return "Tomate Cherry — Etapa: Crecimiento (Growing)";
    }

    private String getNextIrrigationTime(int greenhouseId) {
        return "Próximo lunes a las 06:00 AM — Zona: ZONE_A";
    }

    private void activateIrrigationActuator(int greenhouseId) {
        System.out.printf("[Actuator] Bomba de riego activada en invernadero #%d%n", greenhouseId);
    }

    private void persistReading(int sensorId, SensorType type, double value, int greenhouseId) {
        System.out.printf("[DB] Persistiendo: sensor=%d, type=%s, value=%.2f%n", sensorId, type, value);
    }

    private void evaluateAndGenerateAlert(int greenhouseId, SensorType type, double value) {
        // Umbrales de ejemplo
        if (type == SensorType.TEMPERATURE && value > 35.0) {
            System.out.printf("[Alert] CRÍTICO: Temperatura alta (%.1f°C) en invernadero #%d%n",
                    value, greenhouseId);
        }
    }

    private void logAction(int greenhouseId, String message) {
        System.out.printf("[Log] Invernadero #%d: %s%n", greenhouseId, message);
    }
}
