package com.agropulse.structure.linkedlist;

import com.agropulse.model.enums.SensorType;

/**
 * ESTRUCTURA: LISTA SIMPLE ENLAZADA de lecturas de sensor.
 * PDF Listas Simples: "Coleccion donde cada elemento tiene un sucesor,
 * excepto el ultimo que no tiene sucesor".
 *
 * Uso en AgroPulse: historial de lecturas de un sensor especifico,
 * donde cada lectura apunta a la siguiente cronologicamente.
 *
 * Operaciones: buscar, agregar, eliminar, modificar, acceder, organizar.
 */
public class SensorLinkedList {

    private SensorNode head; // Cabeza de la lista (primer nodo)
    private int size;

    public SensorLinkedList() { this.head = null; this.size = 0; }

    /** Agregar al inicio de la lista. */
    public void addFirst(double value, SensorType type, String timestamp) {
        SensorNode newNode = new SensorNode(value, type, timestamp);
        newNode.next = head;
        head = newNode;
        size++;
    }

    /** Agregar al final de la lista. */
    public void addLast(double value, SensorType type, String timestamp) {
        SensorNode newNode = new SensorNode(value, type, timestamp);
        if (head == null) { head = newNode; size++; return; }
        SensorNode current = head;
        while (current.next != null) current = current.next;
        current.next = newNode;
        size++;
    }

    /** Buscar un nodo por valor. Retorna true si existe. */
    public boolean contains(double value) {
        SensorNode current = head;
        while (current != null) {
            if (Math.abs(current.value - value) < 0.001) return true;
            current = current.next;
        }
        return false;
    }

    /** Eliminar el primer nodo con el valor dado. */
    public boolean remove(double value) {
        if (head == null) return false;
        if (Math.abs(head.value - value) < 0.001) { head = head.next; size--; return true; }
        SensorNode current = head;
        while (current.next != null) {
            if (Math.abs(current.next.value - value) < 0.001) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** Calcular promedio de todos los valores en la lista. */
    public double average() {
        if (head == null) return 0;
        double sum = 0; int count = 0;
        SensorNode current = head;
        while (current != null) { sum += current.value; count++; current = current.next; }
        return sum / count;
    }

    /** Obtener el valor maximo. */
    public double max() {
        if (head == null) throw new IllegalStateException("Lista vacia");
        double max = head.value;
        SensorNode current = head.next;
        while (current != null) { if (current.value > max) max = current.value; current = current.next; }
        return max;
    }

    public int size()      { return size; }
    public boolean isEmpty(){ return head == null; }
    public SensorNode getHead() { return head; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SensorLinkedList -> ");
        SensorNode current = head;
        while (current != null) {
            sb.append(current).append(current.next != null ? " -> " : "");
            current = current.next;
        }
        return sb.toString();
    }
}