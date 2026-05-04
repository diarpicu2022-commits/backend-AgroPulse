package com.agropulse.structure.circulardoublylinkedlist;

/**
 * Nodo de la lista circular doble.
 * PDF Listas Circulares Dobles: "Cada nodo tiene dos punteros.
 * El ULTIMO nodo apunta al PRIMER nodo (circularidad)".
 */
public class ScheduleNode {
    String zoneName;         // Nombre de la zona de riego
    int durationMinutes;     // Duracion del riego en minutos
    String startHour;        // Hora de inicio (ej: "06:00")

    ScheduleNode next;       // Apunta al siguiente (o al primero si es el ultimo)
    ScheduleNode previous;   // Apunta al anterior (o al ultimo si es el primero)

    public ScheduleNode(String zoneName, int durationMinutes, String startHour) {
        this.zoneName        = zoneName;
        this.durationMinutes = durationMinutes;
        this.startHour       = startHour;
        this.next            = null;
        this.previous        = null;
    }

    @Override public String toString() {
        return String.format("Schedule[%s, %dmin, %s]", zoneName, durationMinutes, startHour);
    }
}