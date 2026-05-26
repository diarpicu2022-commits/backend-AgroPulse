package com.agropulse.pattern.behavioral.observer;

import com.agropulse.dao.AlertRepository;
import com.agropulse.dao.SensorThresholdRepository;
import com.agropulse.model.Alert;
import com.agropulse.model.SensorReading;
import com.agropulse.model.SensorThreshold;
import com.agropulse.model.enums.AlertLevel;
import com.agropulse.model.enums.SensorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * PATRÓN OBSERVER — Reacciona a lecturas de sensores: si el valor supera los
 * umbrales configurados (SensorThreshold) genera una alerta real en base de datos.
 * Si no hay umbral configurado aplica valores por defecto de seguridad.
 */
@Component
public class AlertObserver implements IGreenhouseObserver {

    @Autowired private AlertRepository           alertRepository;
    @Autowired private SensorThresholdRepository thresholdRepository;

    @Override
    public void onSensorReading(SensorReading r) {
        try {
            Optional<SensorThreshold> opt = thresholdRepository.findBySensorId(r.getSensorId());
            if (opt.isPresent()) {
                SensorThreshold t = opt.get();
                if (!t.isActive()) return;
                if (t.getMinValue() != null && r.getValue() < t.getMinValue()) {
                    saveAlert(r,
                        sensorLabel(r.getSensorType()) + " por debajo del mínimo: "
                            + String.format("%.1f", r.getValue())
                            + " (mín " + String.format("%.1f", t.getMinValue()) + ")",
                        AlertLevel.WARNING);
                } else if (t.getMaxValue() != null && r.getValue() > t.getMaxValue()) {
                    AlertLevel level = (r.getSensorType() == SensorType.TEMPERATURE
                            && r.getValue() > t.getMaxValue() + 5)
                            ? AlertLevel.CRITICAL : AlertLevel.WARNING;
                    saveAlert(r,
                        sensorLabel(r.getSensorType()) + " por encima del máximo: "
                            + String.format("%.1f", r.getValue())
                            + " (máx " + String.format("%.1f", t.getMaxValue()) + ")",
                        level);
                }
            } else {
                // Umbrales por defecto cuando no hay configuración para el sensor
                if (r.getSensorType() == SensorType.TEMPERATURE && r.getValue() > 35)
                    saveAlert(r, "Temperatura alta: " + String.format("%.1f", r.getValue()) + " °C",
                        AlertLevel.WARNING);
                else if (r.getSensorType() == SensorType.HUMIDITY && r.getValue() < 30)
                    saveAlert(r, "Humedad crítica: " + String.format("%.1f", r.getValue()) + " %",
                        AlertLevel.WARNING);
            }
        } catch (Exception ignored) {
            // No interrumpir el flujo de guardado de lecturas si falla la alerta
        }
    }

    private void saveAlert(SensorReading r, String message, AlertLevel level) {
        Alert alert = new Alert(message, level, r.getGreenhouseId());
        alert.setType("THRESHOLD_VIOLATION");
        alert.setTitle("Alerta de sensor");
        alertRepository.save(alert);
    }

    private String sensorLabel(SensorType type) {
        if (type == null) return "Sensor";
        return switch (type) {
            case TEMPERATURE, TEMPERATURE_INTERNAL, TEMPERATURE_EXTERNAL -> "Temperatura";
            case HUMIDITY, HUMIDITY_INTERNAL, HUMIDITY_EXTERNAL -> "Humedad";
            case SOIL_MOISTURE    -> "Humedad del suelo";
            case CO2              -> "CO₂";
            case LIGHT            -> "Luminosidad";
            case PH               -> "pH";
            case WIND_SPEED       -> "Viento";
        };
    }

    @Override public String getObserverName() { return "AlertObserver"; }
}