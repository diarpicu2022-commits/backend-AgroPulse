package com.agropulse.structure.doublylinkedlist;

import com.agropulse.model.enums.SensorType;

/**
 * Nodo de la lista doblemente enlazada.
 * PDF Listas Dobles: "Cada nodo contiene un valor y DOS punteros:
 * uno al nodo siguiente y otro al nodo anterior".
 */
public class ReadingNode {
    double value;
    SensorType type;
    String timestamp;
    ReadingNode next;     // Puntero al nodo siguiente
    ReadingNode previous; // Puntero al nodo anterior — lo que la diferencia de lista simple

    public ReadingNode(double value, SensorType type, String timestamp) {
        this.value     = value;
        this.type      = type;
        this.timestamp = timestamp;
        this.next      = null;
        this.previous  = null;
    }

    @Override public String toString() {
        return String.format("DNode[%.1f %s @ %s]", value, type, timestamp);
    }
}