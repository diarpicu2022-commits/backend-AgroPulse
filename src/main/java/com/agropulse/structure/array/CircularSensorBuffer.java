package com.agropulse.structure.array;

/**
 * ESTRUCTURA: ARRAY CIRCULAR de lecturas de sensor.
 * PDF Arrays: "El final del array esta conectado al principio, creando un ciclo continuo".
 *
 * Uso en AgroPulse: buffer de las ultimas N lecturas de un sensor en tiempo real.
 * Cuando el buffer se llena, las lecturas mas antiguas se sobreescriben automaticamente.
 */
public class CircularSensorBuffer {

    private final double[] buffer;   // Array circular
    private int head;                // Indice del elemento mas antiguo
    private int tail;                // Indice donde insertar el siguiente
    private int count;               // Cantidad actual de elementos
    private final int capacity;

    public CircularSensorBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer   = new double[capacity];
        this.head     = 0;
        this.tail     = 0;
        this.count    = 0;
    }

    /** Insertar lectura. Si el buffer esta lleno, sobreescribe la mas antigua. */
    public void write(double value) {
        buffer[tail] = value;
        tail = (tail + 1) % capacity; // Avance circular: al llegar al fin, vuelve al inicio
        if (count < capacity) {
            count++;
        } else {
            head = (head + 1) % capacity; // Desplazar inicio (la lectura mas antigua se descarta)
        }
    }

    /** Leer la lectura mas antigua (sin eliminarla). */
    public double peek() {
        if (count == 0) throw new IllegalStateException("Buffer circular vacio");
        return buffer[head];
    }

    /** Calcular promedio de todas las lecturas en el buffer. */
    public double average() {
        if (count == 0) return 0;
        double sum = 0;
        for (int i = 0; i < count; i++) sum += buffer[(head + i) % capacity];
        return sum / count;
    }

    /** Obtener la lectura mas reciente. */
    public double latest() {
        if (count == 0) throw new IllegalStateException("Buffer circular vacio");
        return buffer[(tail - 1 + capacity) % capacity];
    }

    public boolean isFull()  { return count == capacity; }
    public boolean isEmpty() { return count == 0; }
    public int size()        { return count; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CircularBuffer[");
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%.1f", buffer[(head+i)%capacity]));
            if (i < count-1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}