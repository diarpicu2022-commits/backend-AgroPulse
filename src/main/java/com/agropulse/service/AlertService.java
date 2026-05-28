package com.agropulse.service;

import com.agropulse.dao.AlertRepository;
import com.agropulse.model.Alert;
import com.agropulse.model.enums.AlertLevel;
import com.agropulse.structure.stack.AlertStack;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  INTEGRACIÓN REAL: AlertStack (LIFO) + AlertRepository          ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Checklist de Conformidad:                                       ║
 * ║  ¿Qué estructura?  PILA (Stack) — AlertStack<Alert>             ║
 * ║  ¿Por qué LIFO?    La última alerta creada es la más urgente.   ║
 * ║                    El operario atiende "la más nueva primero".   ║
 * ║  ¿Qué patrón?      SRP (SOLID) — única responsabilidad:         ║
 * ║                    gestión del ciclo de vida de alertas.         ║
 * ║  ¿Stack envuelve la BD?  Sí — cada save() en BD hace push()     ║
 * ║                           en la pila en memoria.                 ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Service
public class AlertService {

    private final AlertRepository alertRepository;

    // ESTRUCTURA DE DATOS: Pila LIFO de alertas en memoria
    // La pila complementa la BD: la BD persiste, la pila ordena LIFO
    private final AlertStack alertStack = new AlertStack();

    @Autowired
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Precarga el AlertStack desde BD al arrancar el servidor.
     * Solo carga alertas CRITICAL/WARNING para no saturar memoria.
     * Garantiza que la pila LIFO tenga datos reales desde el inicio.
     */
    @PostConstruct
    public void warmupStack() {
        List<Alert> recent = alertRepository.findAllByOrderByCreatedAtDesc();
        // Carga las últimas 20 alertas urgentes — más recientes primero (LIFO)
        recent.stream()
              .filter(a -> a.getLevel() == AlertLevel.CRITICAL
                        || a.getLevel() == AlertLevel.WARNING)
              .limit(20)
              .forEach(alertStack::push);  // ESTRUCTURA: push LIFO
    }

    // ── createAlert: persiste en BD + apila en LIFO ───────────────────
    /**
     * OPERACIÓN PUSH: al crear una alerta, se apila en la cima de la pila.
     * La alerta más reciente queda siempre encima — orden LIFO garantizado.
     */
    public Alert createAlert(Alert alert) {
        Alert saved = alertRepository.save(alert);
        alertStack.push(saved);   // ESTRUCTURA: push() — LIFO
        return saved;
    }

    // ── getAllAlerts: lectura ordenada de BD ──────────────────────────
    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    // ── markRead: actualiza estado sin tocar la pila ──────────────────
    public Optional<Alert> markRead(int id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setRead(true);
            return alertRepository.save(alert);
        });
    }

    public void deleteAlert(int id) {
        alertRepository.deleteById(id);
    }

    public boolean existsAlert(int id) {
        return alertRepository.existsById(id);
    }

    // ── getStackStatus: estado de la pila LIFO ────────────────────────
    /**
     * Expone el estado interno de la PILA LIFO sin extraer elementos.
     * Permite al cliente conocer: tamaño, cima, y si hay críticas pendientes.
     */
    public Map<String, Object> getStackStatus() {
        return Map.of(
            "estructura",    "PILA — LIFO (último en entrar, primero en salir)",
            "operaciones",   "push | pop | peek | isEmpty",
            "tamaño",        alertStack.size(),
            "vacía",         alertStack.isEmpty(),
            "tieneCrítica",  alertStack.hasTopLevelCritical(),
            "cima",          alertStack.isEmpty()
                             ? "pila vacía"
                             : "[" + alertStack.peek().getLevel() + "] " + alertStack.peek().getMessage()
        );
    }

    // ── drainCritical: pop LIFO de todas las alertas CRITICAL ─────────
    /**
     * OPERACIÓN POP en cadena: extrae y devuelve todas las alertas CRITICAL
     * de la cima de la pila hasta encontrar una no-crítica.
     *
     * Caso de uso: el sistema de notificaciones consume solo las CRITICAL
     * en el orden LIFO — las más recientes se atienden primero.
     */
    public List<Alert> drainCriticalAlerts() {
        return alertStack.drainCritical();  // ESTRUCTURA: pop() iterativo LIFO
    }

    // ── peekTop: ver cima sin extraer ────────────────────────────────
    public Optional<Alert> peekTop() {
        if (alertStack.isEmpty()) return Optional.empty();
        return Optional.of(alertStack.peek());  // ESTRUCTURA: peek() LIFO
    }
}
