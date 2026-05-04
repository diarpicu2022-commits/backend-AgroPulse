package com.agropulse.structure.queue;

import com.agropulse.pattern.creational.builder.IrrigationSchedule;
import java.util.LinkedList;
import java.util.Deque;

/**
 * ESTRUCTURA: COLA (FIFO) de ordenes de riego.
 * PDF Colas: "Primer elemento en entrar, primero en salir (FIFO)".
 *
 * Uso en AgroPulse: las ordenes de riego se procesan en el orden en que
 * se programaron. La primera zona programada se riega primero.
 *
 * Operaciones: enqueue, dequeue, front, rear, size.
 * Casos de uso reales (PDF): programacion de tareas, manejo de eventos,
 * simulacion de sistemas, programacion concurrente.
 */
public class IrrigationQueue implements IQueue<IrrigationSchedule> {

    private final Deque<IrrigationSchedule> queue = new LinkedList<>();

    // ── enqueue: agregar orden de riego al final ──────────────────────
    @Override
    public void enqueue(IrrigationSchedule schedule) {
        queue.addLast(schedule);
        System.out.printf("[IrrigationQueue] enqueue: %s%n", schedule.getScheduleName());
    }

    // ── dequeue: extraer la orden mas antigua (la primera programada) ─
    @Override
    public IrrigationSchedule dequeue() {
        if (isEmpty()) throw new IllegalStateException("Cola de riego vacia");
        IrrigationSchedule next = queue.removeFirst();
        System.out.printf("[IrrigationQueue] dequeue: %s%n", next.getScheduleName());
        return next;
    }

    // ── front: ver la proxima orden sin extraerla ─────────────────────
    @Override
    public IrrigationSchedule front() {
        if (isEmpty()) throw new IllegalStateException("Cola de riego vacia");
        return queue.peekFirst();
    }

    // ── rear: ver la ultima orden en espera ───────────────────────────
    @Override
    public IrrigationSchedule rear() {
        if (isEmpty()) throw new IllegalStateException("Cola de riego vacia");
        return queue.peekLast();
    }

    @Override public boolean isEmpty() { return queue.isEmpty(); }
    @Override public int size()        { return queue.size(); }

    /** Procesa todas las ordenes en la cola (simulacion). */
    public void processAll() {
        System.out.println("[IrrigationQueue] Procesando " + size() + " ordenes de riego...");
        while (!isEmpty()) {
            IrrigationSchedule schedule = dequeue();
            System.out.printf("  Ejecutando: %s en zona %s%n",
                    schedule.getScheduleName(), schedule.getIrrigationZone());
        }
    }

    @Override
    public String toString() {
        return "IrrigationQueue{size=" + size() +
               (isEmpty() ? "" : ", next='" + front().getScheduleName() + "'") + "}";
    }
}