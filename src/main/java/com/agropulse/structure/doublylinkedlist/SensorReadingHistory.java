package com.agropulse.structure.doublylinkedlist;

import com.agropulse.model.enums.SensorType;

/**
 * ESTRUCTURA: LISTA DOBLEMENTE ENLAZADA de historial de lecturas.
 * PDF Listas Dobles: "Estructura donde cada nodo tiene dos punteros,
 * uno al siguiente y otro al anterior".
 *
 * Ventajas (PDF): recorrido bidireccional, insercion/eliminacion eficiente
 * en cualquier posicion.
 *
 * Uso en AgroPulse: el operario puede navegar el historial hacia adelante
 * (lecturas mas recientes) y hacia atras (lecturas mas antiguas).
 *
 * Conceptos implementados:
 *   - Cabeza (head): primera lectura cronologica
 *   - Cola (tail): ultima lectura (mas reciente)
 *   - Recorrido bidireccional
 */
public class SensorReadingHistory {

    private ReadingNode head; // Primera lectura (la mas antigua)
    private ReadingNode tail; // Ultima lectura (la mas reciente)
    private int size;

    public SensorReadingHistory() { this.head = null; this.tail = null; this.size = 0; }

    /** Agregar lectura al final (la mas reciente va al final). */
    public void addLast(double value, SensorType type, String timestamp) {
        ReadingNode newNode = new ReadingNode(value, type, timestamp);
        if (tail == null) {
            head = tail = newNode;
        } else {
            newNode.previous = tail;
            tail.next        = newNode;
            tail             = newNode;
        }
        size++;
    }

    /** Agregar lectura al inicio (insercion al principio). */
    public void addFirst(double value, SensorType type, String timestamp) {
        ReadingNode newNode = new ReadingNode(value, type, timestamp);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next     = head;
            head.previous    = newNode;
            head             = newNode;
        }
        size++;
    }

    /** Recorrer hacia adelante (de antigua a reciente) — Ventaja de lista doble. */
    public String traverseForward() {
        StringBuilder sb = new StringBuilder("HEAD -> ");
        ReadingNode current = head;
        while (current != null) {
            sb.append(current).append(current.next != null ? " <-> " : " <- TAIL");
            current = current.next;
        }
        return sb.toString();
    }

    /** Recorrer hacia atras (de reciente a antigua) — Ventaja de lista doble. */
    public String traverseBackward() {
        StringBuilder sb = new StringBuilder("TAIL -> ");
        ReadingNode current = tail;
        while (current != null) {
            sb.append(current).append(current.previous != null ? " <-> " : " <- HEAD");
            current = current.previous;
        }
        return sb.toString();
    }

    /** Eliminar la lectura mas antigua (cabeza). */
    public ReadingNode removeFirst() {
        if (head == null) throw new IllegalStateException("Historial vacio");
        ReadingNode removed = head;
        head = head.next;
        if (head != null) head.previous = null;
        else tail = null;
        size--;
        return removed;
    }

    /** Eliminar la lectura mas reciente (cola). */
    public ReadingNode removeLast() {
        if (tail == null) throw new IllegalStateException("Historial vacio");
        ReadingNode removed = tail;
        tail = tail.previous;
        if (tail != null) tail.next = null;
        else head = null;
        size--;
        return removed;
    }

    /** Calcular promedio de todos los valores. */
    public double average() {
        if (size == 0) return 0;
        double sum = 0;
        ReadingNode current = head;
        while (current != null) { sum += current.value; current = current.next; }
        return sum / size;
    }

    public int size()       { return size; }
    public boolean isEmpty(){ return size == 0; }
    public ReadingNode getHead() { return head; }
    public ReadingNode getTail() { return tail; }
}