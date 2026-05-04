package com.agropulse.structure.linkedlist;

import com.agropulse.model.enums.SensorType;

/**
 * Nodo de la lista simple enlazada.
 * PDF Listas Simples: "Cada elemento tiene un campo de valor y un puntero al siguiente".
 */
public class SensorNode {
    // Datos del nodo
    double value;
    SensorType type;
    String timestamp;

    // Puntero al siguiente nodo — PDF: "sucesor"
    SensorNode next;

    public SensorNode(double value, SensorType type, String timestamp) {
        this.value     = value;
        this.type      = type;
        this.timestamp = timestamp;
        this.next      = null; // El ultimo nodo no tiene sucesor
    }

    @Override public String toString() {
        return String.format("Node[%.1f %s @ %s]", value, type, timestamp);
    }
}