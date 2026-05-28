package com.agropulse.service;

import com.agropulse.pattern.creational.builder.IrrigationSchedule;
import com.agropulse.structure.queue.IrrigationQueue;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  INTEGRACIÓN REAL: IrrigationQueue (FIFO) + Builder             ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Checklist de Conformidad:                                       ║
 * ║  ¿Qué estructura?  COLA (Queue) — IrrigationQueue FIFO          ║
 * ║  ¿Por qué FIFO?    Las zonas programadas primero se riegan       ║
 * ║                    primero — orden justo de servicio.            ║
 * ║  ¿Qué patrón?      Builder — IrrigationSchedule.Builder()        ║
 * ║                    para construir horarios complejos paso a paso. ║
 * ║  ¿SRP?             Sí — única responsabilidad: gestión de la     ║
 * ║                    cola de programación de riegos.               ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Service
public class IrrigationService {

    // ESTRUCTURA DE DATOS: Cola FIFO de horarios de riego
    // Singleton de aplicación — Spring gestiona la instancia única por contexto
    private final IrrigationQueue irrigationQueue = new IrrigationQueue();

    // ── enqueue: agregar horario al final de la cola ──────────────────
    /**
     * OPERACIÓN ENQUEUE: construye un IrrigationSchedule con el Builder
     * y lo encola al final de la cola FIFO.
     *
     * Combina dos patrones: Builder (crea el objeto complejo)
     * + Queue (lo encola en orden de llegada).
     */
    public IrrigationSchedule enqueueSchedule(int greenhouseId, String zoneName,
                                               int startHour, int startMinute,
                                               int durationMinutes, double targetMoisture) {
        // PATRÓN BUILDER: construcción paso a paso del horario complejo
        IrrigationSchedule schedule = new IrrigationSchedule.Builder(
                "Riego " + zoneName + " - GH#" + greenhouseId, greenhouseId)
                .irrigationZone(zoneName)
                .startTime(LocalTime.of(startHour, startMinute))
                .durationMinutes(durationMinutes)
                .targetSoilMoisture(targetMoisture)
                .activeDays(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .autoStopOnRain(true)
                .sensorTriggered(targetMoisture > 0)
                .build();

        irrigationQueue.enqueue(schedule);  // ESTRUCTURA: enqueue() FIFO
        return schedule;
    }

    // ── processNext: dequeue del frente (FIFO) ────────────────────────
    /**
     * OPERACIÓN DEQUEUE: extrae y "ejecuta" el primer horario en la cola.
     * El primero en entrar es el primero en ser procesado — FIFO.
     */
    public Map<String, Object> processNext() {
        if (irrigationQueue.isEmpty()) {
            return Map.of(
                "mensaje",    "La cola de riego está vacía",
                "pendientes", 0
            );
        }
        IrrigationSchedule next = irrigationQueue.dequeue();  // ESTRUCTURA: dequeue() FIFO
        return Map.of(
            "operacion",    "DEQUEUE — primer elemento extraído de la cola FIFO",
            "procesado",    next.getScheduleName(),
            "zona",         next.getIrrigationZone(),
            "inicio",       next.getStartTime().toString(),
            "duración",     next.getDurationMinutes() + " min",
            "objetivo",     next.getTargetSoilMoisture() + "% humedad",
            "pendientes",   irrigationQueue.size()
        );
    }

    // ── processAll: vaciar toda la cola ──────────────────────────────
    /**
     * Procesa todos los horarios pendientes en orden FIFO.
     * Devuelve resumen de todo lo procesado.
     */
    public Map<String, Object> processAll() {
        int total = irrigationQueue.size();
        if (total == 0) return Map.of("mensaje", "Cola ya estaba vacía", "procesados", 0);

        List<String> processed = new ArrayList<>();
        while (!irrigationQueue.isEmpty()) {
            IrrigationSchedule s = irrigationQueue.dequeue();  // ESTRUCTURA: dequeue() FIFO
            processed.add(s.getScheduleName() + " → zona " + s.getIrrigationZone());
        }
        return Map.of(
            "operacion",  "DEQUEUE total — cola FIFO vaciada en orden de llegada",
            "procesados", total,
            "detalle",    processed
        );
    }

    // ── getQueueStatus: estado sin extraer (front/rear) ───────────────
    /**
     * Consulta el estado de la cola sin modificarla.
     * Expone: tamaño, próximo a procesar (front), último encolado (rear).
     *
     * ESTRUCTURA: front() + rear() — consultas no destructivas FIFO.
     */
    public Map<String, Object> getQueueStatus() {
        if (irrigationQueue.isEmpty()) {
            return Map.of(
                "estructura", "COLA — FIFO (primero en entrar, primero en salir)",
                "tamaño",     0,
                "vacía",      true
            );
        }
        return Map.of(
            "estructura",   "COLA — FIFO (primero en entrar, primero en salir)",
            "operaciones",  "enqueue | dequeue | front | rear | isEmpty",
            "tamaño",       irrigationQueue.size(),
            "vacía",        false,
            "próximo",      irrigationQueue.front().getScheduleName(),   // ESTRUCTURA: front()
            "último",       irrigationQueue.rear().getScheduleName()     // ESTRUCTURA: rear()
        );
    }
}
