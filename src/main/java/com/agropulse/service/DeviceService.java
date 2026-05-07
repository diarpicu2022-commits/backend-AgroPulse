package com.agropulse.service;

import com.agropulse.dao.ActuatorRepository;
import com.agropulse.dao.SensorRepository;
import com.agropulse.model.Actuator;
import com.agropulse.model.Sensor;
import com.agropulse.model.enums.SensorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Servicio de registro y configuración de dispositivos ESP32.
 *
 * Soporta dos caminos de configuración:
 *   Camino A — Admin Panel: admin crea sensores/actuadores en el frontend.
 *   Camino B — ESP32 Portal: el dispositivo registra su propia config vía POST /device/register.
 *
 * En ambos casos el backend es la fuente de verdad. El ESP32 descarga su
 * configuración con GET /device/config/{greenhouseId} al arrancar.
 */
@Service
public class DeviceService {

    @Autowired private SensorRepository   sensorRepository;
    @Autowired private ActuatorRepository actuatorRepository;

    // ── Camino B: ESP32 registra su configuración ─────────────────────────
    @Transactional
    public Map<String, Object> registerDevice(int greenhouseId, String deviceId,
                                              List<Map<String, Object>> sensors,
                                              List<Map<String, Object>> actuators) {
        // Elimina el registro anterior de este dispositivo (no toca los del admin)
        sensorRepository.deleteByGreenhouseIdAndDeviceSource(greenhouseId, deviceId);
        actuatorRepository.deleteByGreenhouseIdAndDeviceSource(greenhouseId, deviceId);

        List<Map<String, Object>> sensorIds   = new ArrayList<>();
        List<Map<String, Object>> actuatorIds = new ArrayList<>();

        for (int i = 0; i < sensors.size(); i++) {
            Map<String, Object> s = sensors.get(i);
            Sensor sensor = new Sensor();
            sensor.setName((String) s.getOrDefault("name", "Sensor " + i));
            sensor.setGreenhouseId(greenhouseId);
            sensor.setDeviceSource(deviceId);
            sensor.setType(parseSensorType((String) s.get("type")));
            if (s.containsKey("gpioPin"))  sensor.setGpioPin(toInt(s.get("gpioPin")));
            if (s.containsKey("protocol")) sensor.setProtocol((String) s.get("protocol"));
            if (s.containsKey("location")) sensor.setLocation((String) s.get("location"));
            sensorRepository.save(sensor);
            sensorIds.add(Map.of("localIndex", i, "id", sensor.getId()));
        }

        for (int i = 0; i < actuators.size(); i++) {
            Map<String, Object> a = actuators.get(i);
            Actuator actuator = new Actuator();
            actuator.setName((String) a.getOrDefault("name", "Actuador " + i));
            actuator.setGreenhouseId(greenhouseId);
            actuator.setDeviceSource(deviceId);
            if (a.containsKey("type"))      actuator.setType((String) a.get("type"));
            if (a.containsKey("gpioPin"))   actuator.setGpioPin(toInt(a.get("gpioPin")));
            if (a.containsKey("activeLow")) actuator.setActiveLow(toBool(a.get("activeLow")));
            actuatorRepository.save(actuator);
            actuatorIds.add(Map.of("localIndex", i, "id", actuator.getId()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("greenhouseId", greenhouseId);
        result.put("deviceId",     deviceId);
        result.put("sensors",      sensorIds);
        result.put("actuators",    actuatorIds);
        return result;
    }

    // ── Camino A + B: ESP32 descarga su configuración al arrancar ─────────
    public Map<String, Object> getDeviceConfig(int greenhouseId) {
        List<Sensor>   sensors   = sensorRepository.findByGreenhouseId(greenhouseId);
        List<Actuator> actuators = actuatorRepository.findByGreenhouseId(greenhouseId);

        List<Map<String, Object>> sensorMaps = sensors.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           s.getId());
            m.put("name",         s.getName());
            m.put("type",         s.getType().name());
            m.put("gpioPin",      s.getGpioPin());
            m.put("protocol",     s.getProtocol());
            m.put("location",     s.getLocation());
            m.put("deviceSource", s.getDeviceSource());
            m.put("active",       s.isActive());
            return m;
        }).toList();

        List<Map<String, Object>> actuatorMaps = actuators.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           a.getId());
            m.put("name",         a.getName());
            m.put("type",         a.getType());
            m.put("gpioPin",      a.getGpioPin());
            m.put("activeLow",    a.isActiveLow());
            m.put("status",       a.getStatus());
            m.put("deviceSource", a.getDeviceSource());
            m.put("active",       a.isActive());
            return m;
        }).toList();

        // GPIOs ya utilizados en este invernadero (para el picker del frontend)
        Set<Integer> usedGpios = new LinkedHashSet<>();
        sensors.stream()  .filter(s -> s.getGpioPin() != null).forEach(s -> usedGpios.add(s.getGpioPin()));
        actuators.stream().filter(a -> a.getGpioPin() != null).forEach(a -> usedGpios.add(a.getGpioPin()));

        String deviceId = sensors.stream()
            .map(Sensor::getDeviceSource).filter(Objects::nonNull).findFirst()
            .orElse(actuators.stream()
                .map(Actuator::getDeviceSource).filter(Objects::nonNull).findFirst()
                .orElse(""));

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("greenhouseId", greenhouseId);
        config.put("deviceId",     deviceId);
        config.put("sensors",      sensorMaps);
        config.put("actuators",    actuatorMaps);
        config.put("usedGpios",    new ArrayList<>(usedGpios));
        return config;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private SensorType parseSensorType(String typeStr) {
        if (typeStr == null) return SensorType.TEMPERATURE;
        try { return SensorType.valueOf(typeStr.toUpperCase()); }
        catch (IllegalArgumentException e) { return SensorType.TEMPERATURE; }
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long)    return ((Long) value).intValue();
        if (value instanceof Double)  return ((Double) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private boolean toBool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
}
