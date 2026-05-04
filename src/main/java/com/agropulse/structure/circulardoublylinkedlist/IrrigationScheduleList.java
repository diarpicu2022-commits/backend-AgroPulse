package com.agropulse.structure.circulardoublylinkedlist;

/**
 * ESTRUCTURA: LISTA CIRCULAR DOBLE de programas de riego.
 * PDF Listas Circulares Dobles: "El ultimo nodo apunta al primer nodo,
 * creando una estructura circular con recorrido bidireccional".
 *
 * Ventajas (PDF):
 *   - Recorrido bidireccional: puede recorrer zonas de riego en ambos sentidos
 *   - Circularidad: al terminar la ultima zona, vuelve automaticamente a la primera
 *   - Mayor seguridad en acceso a nodos: verifica punteros nulos
 *
 * Uso en AgroPulse: los programas de riego rotan ciclicamente.
 * Al terminar el riego de la ultima zona, el sistema automaticamente
 * continua con la primera zona del siguiente ciclo.
 *
 * Analogia del PDF: como un reloj (taller de Listas Circulares Dobles).
 */
public class IrrigationScheduleList {

    private ScheduleNode head;  // Primer nodo (primera zona de riego)
    private ScheduleNode tail;  // Ultimo nodo (apunta de vuelta a head — circularidad)
    private ScheduleNode current; // Cursor para la rotacion ciclica
    private int size;

    public IrrigationScheduleList() {
        this.head    = null;
        this.tail    = null;
        this.current = null;
        this.size    = 0;
    }

    /** Agregar zona de riego al final del ciclo. */
    public void addZone(String zoneName, int durationMinutes, String startHour) {
        ScheduleNode newNode = new ScheduleNode(zoneName, durationMinutes, startHour);

        if (head == null) {
            head          = newNode;
            tail          = newNode;
            newNode.next     = head; // El unico nodo apunta a si mismo (circularidad)
            newNode.previous = head;
            current          = head;
        } else {
            // Insertar antes de head (al final del ciclo)
            newNode.next     = head;       // El nuevo ultimo apunta al primero (circularidad)
            newNode.previous = tail;
            tail.next        = newNode;
            head.previous    = newNode;
            tail             = newNode;
        }
        size++;
        System.out.printf("[CircularList] Zona agregada: %s%n", newNode);
    }

    /**
     * Avanzar al siguiente programa de riego.
     * Gracias a la circularidad, nunca llega a null.
     * PDF: "Recorrido bidireccional desde el principio hasta el final o desde el final".
     */
    public ScheduleNode next() {
        if (current == null) throw new IllegalStateException("Lista de zonas vacia");
        ScheduleNode result = current;
        current = current.next; // Avanza (si es el ultimo, regresa al primero automaticamente)
        return result;
    }

    /** Retroceder al programa anterior (recorrido inverso). */
    public ScheduleNode previous() {
        if (current == null) throw new IllegalStateException("Lista de zonas vacia");
        current = current.previous;
        return current;
    }

    /** Recorrer TODAS las zonas una vez y regresar al inicio. */
    public String traverseOneCycle() {
        if (head == null) return "Lista de zonas vacia";
        StringBuilder sb  = new StringBuilder("Ciclo de riego: ");
        ScheduleNode cursor = head;
        int count = 0;
        do {
            sb.append(cursor.zoneName);
            cursor = cursor.next;
            if (++count < size) sb.append(" -> ");
        } while (cursor != head && count < size); // Detener al volver al inicio
        sb.append(" -> (vuelve a ").append(head.zoneName).append(")");
        return sb.toString();
    }

    /** Eliminar una zona del ciclo de riego. */
    public boolean removeZone(String zoneName) {
        if (head == null) return false;
        ScheduleNode cursor = head;
        int count = 0;
        do {
            if (cursor.zoneName.equals(zoneName)) {
                if (size == 1) { head = tail = current = null; size--; return true; }
                cursor.previous.next = cursor.next;
                cursor.next.previous = cursor.previous;
                if (cursor == head) head = cursor.next;
                if (cursor == tail) tail = cursor.previous;
                if (cursor == current) current = cursor.next;
                size--;
                return true;
            }
            cursor = cursor.next;
        } while (cursor != head && ++count < size);
        return false;
    }

    public int size()       { return size; }
    public boolean isEmpty(){ return size == 0; }
    public ScheduleNode getHead() { return head; }

    @Override
    public String toString() {
        return "IrrigationScheduleList{zonas=" + size + ", ciclo=" + traverseOneCycle() + "}";
    }
}