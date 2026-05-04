package com.agropulse.structure.stack;

import com.agropulse.model.Alert;
import com.agropulse.model.enums.AlertLevel;
import java.util.ArrayList;
import java.util.List;

/**
 * ESTRUCTURA: PILA (LIFO) de alertas del invernadero.
 * PDF Pilas: "Ultimo en entrar, Primero en salir (LIFO)".
 *
 * Uso en AgroPulse: la alerta mas reciente es la mas critica y se atiende primero.
 * El operario ve las alertas en orden LIFO: las ultimas generadas se muestran arriba.
 *
 * Operaciones implementadas: push, pop, top (peek), isEmpty.
 * Uso: algoritmos de busqueda en profundidad, deshacer acciones, historial.
 */
public class AlertStack implements IStack<Alert> {

    private final List<Alert> stack = new ArrayList<>(); // Lista interna como soporte

    // ── push: agrega alerta a la cima de la pila ─────────────────────
    @Override
    public void push(Alert alert) {
        stack.add(alert);
        System.out.printf("[AlertStack] push: [%s] %s%n", alert.getLevel(), alert.getMessage());
    }

    // ── pop: extrae y devuelve la alerta de la cima (la mas reciente) ─
    @Override
    public Alert pop() {
        if (isEmpty()) throw new IllegalStateException("La pila de alertas esta vacia");
        Alert top = stack.remove(stack.size() - 1);
        System.out.printf("[AlertStack] pop: [%s] %s%n", top.getLevel(), top.getMessage());
        return top;
    }

    // ── peek/top: ve la cima sin extraer ─────────────────────────────
    @Override
    public Alert peek() {
        if (isEmpty()) throw new IllegalStateException("La pila de alertas esta vacia");
        return stack.get(stack.size() - 1);
    }

    @Override public boolean isEmpty() { return stack.isEmpty(); }
    @Override public int size()        { return stack.size(); }

    /** Verifica si la alerta mas reciente es critica. */
    public boolean hasTopLevelCritical() {
        return !isEmpty() && peek().getLevel() == AlertLevel.CRITICAL;
    }

    /** Extrae todas las alertas de nivel CRITICAL. */
    public List<Alert> drainCritical() {
        List<Alert> criticals = new ArrayList<>();
        while (!isEmpty() && peek().getLevel() == AlertLevel.CRITICAL) {
            criticals.add(pop());
        }
        return criticals;
    }

    @Override
    public String toString() {
        return "AlertStack{size=" + size() + ", top=" + (isEmpty() ? "vacia" : peek().getLevel()) + "}";
    }
}