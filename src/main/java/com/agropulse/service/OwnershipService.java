package com.agropulse.service;

import com.agropulse.dao.ActuatorRepository;
import com.agropulse.dao.GreenhouseRepository;
import com.agropulse.dao.SensorRepository;
import com.agropulse.model.Actuator;
import com.agropulse.model.Greenhouse;
import com.agropulse.model.Sensor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Centraliza la verificación de propiedad sobre recursos del sistema.
 * Lee el header X-User-Id enviado por el frontend tras el login y lo compara
 * con el ownerId del recurso, evitando que un usuario modifique recursos ajenos.
 * Los administradores (X-Admin-Email) siempre tienen acceso.
 */
@Service
public class OwnershipService {

    @Autowired private GreenhouseRepository greenhouseRepository;
    @Autowired private SensorRepository     sensorRepository;
    @Autowired private ActuatorRepository   actuatorRepository;

    public boolean isAdmin(HttpServletRequest request) {
        String envAdmin    = System.getenv("AGROPULSE_ADMIN_EMAIL");
        String headerAdmin = request.getHeader("X-Admin-Email");
        return envAdmin != null && envAdmin.equals(headerAdmin);
    }

    public int getRequestUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isBlank()) return 0;
        try { return Integer.parseInt(header.trim()); } catch (NumberFormatException e) { return 0; }
    }

    public boolean canModifyGreenhouse(int greenhouseId, HttpServletRequest request) {
        if (isAdmin(request)) return true;
        int userId = getRequestUserId(request);
        if (userId == 0) return false;
        Optional<Greenhouse> opt = greenhouseRepository.findById(greenhouseId);
        if (opt.isEmpty()) return false;
        int ownerId = opt.get().getOwnerId();
        return ownerId == 0 || ownerId == userId;
    }

    public boolean canModifySensor(int sensorId, HttpServletRequest request) {
        if (isAdmin(request)) return true;
        int userId = getRequestUserId(request);
        if (userId == 0) return false;
        Optional<Sensor> opt = sensorRepository.findById(sensorId);
        if (opt.isEmpty()) return false;
        Optional<Greenhouse> gh = greenhouseRepository.findById(opt.get().getGreenhouseId());
        if (gh.isEmpty()) return false;
        int ownerId = gh.get().getOwnerId();
        return ownerId == 0 || ownerId == userId;
    }

    public boolean canModifyActuator(int actuatorId, HttpServletRequest request) {
        if (isAdmin(request)) return true;
        int userId = getRequestUserId(request);
        if (userId == 0) return false;
        Optional<Actuator> opt = actuatorRepository.findById(actuatorId);
        if (opt.isEmpty()) return false;
        Optional<Greenhouse> gh = greenhouseRepository.findById(opt.get().getGreenhouseId());
        if (gh.isEmpty()) return false;
        int ownerId = gh.get().getOwnerId();
        return ownerId == 0 || ownerId == userId;
    }
}
