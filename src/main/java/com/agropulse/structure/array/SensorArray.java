package com.agropulse.structure.array;

import com.agropulse.model.enums.SensorType;

/**
 * ESTRUCTURA: ARRAY UNIDIMENSIONAL de lecturas de sensores.
 * PDF Arrays: "Un array que tiene una sola dimension".
 * Operaciones implementadas: acceso por indice, insercion (append/insert),
 * eliminacion, busqueda, ordenacion.
 */
public class SensorArray {

    private double[] values;          // Array unidimensional de lecturas
    private SensorType[] types;       // Tipo de cada lectura
    private int size;                 // Cantidad actual de elementos
    private static final int DEFAULT_CAPACITY = 100;

    public SensorArray() {
        this.values = new double[DEFAULT_CAPACITY];
        this.types  = new SensorType[DEFAULT_CAPACITY];
        this.size   = 0;
    }

    public SensorArray(int capacity) {
        this.values = new double[capacity];
        this.types  = new SensorType[capacity];
        this.size   = 0;
    }

    /** Insercion al final (append) — PDF: "Insercion de Elementos". */
    public void append(double value, SensorType type) {
        if (size >= values.length) resize();
        values[size] = value;
        types[size]  = type;
        size++;
    }

    /** Insercion en posicion especifica — PDF: insert. */
    public void insert(int index, double value, SensorType type) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("Indice fuera de rango: " + index);
        if (size >= values.length) resize();
        // Desplazar elementos hacia la derecha
        for (int i = size; i > index; i--) {
            values[i] = values[i-1];
            types[i]  = types[i-1];
        }
        values[index] = value;
        types[index]  = type;
        size++;
    }

    /** Acceso por indice — PDF: "Acceso a Elementos". */
    public double get(int index) {
        validateIndex(index);
        return values[index];
    }

    public SensorType getType(int index) {
        validateIndex(index);
        return types[index];
    }

    /** Eliminacion por valor — PDF: remove. */
    public boolean remove(double value) {
        int index = indexOf(value);
        if (index < 0) return false;
        removeAt(index);
        return true;
    }

    /** Eliminacion por posicion — PDF: del. */
    public void removeAt(int index) {
        validateIndex(index);
        for (int i = index; i < size - 1; i++) {
            values[i] = values[i+1];
            types[i]  = types[i+1];
        }
        size--;
    }

    /** Eliminacion del ultimo elemento — PDF: pop. */
    public double pop() {
        if (size == 0) throw new IllegalStateException("Array vacio");
        double last = values[size-1];
        size--;
        return last;
    }

    /** Busqueda — PDF: index. Retorna -1 si no encuentra. */
    public int indexOf(double value) {
        for (int i = 0; i < size; i++) {
            if (Math.abs(values[i] - value) < 0.001) return i;
        }
        return -1;
    }

    /** Ordenacion ascendente — PDF: sort(a-z). */
    public void sortAscending() {
        // Bubble Sort para ilustrar el concepto (en produccion usar Arrays.sort)
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (values[j] > values[j+1]) {
                    // Intercambiar valores
                    double tmpVal = values[j]; values[j] = values[j+1]; values[j+1] = tmpVal;
                    SensorType tmpType = types[j]; types[j] = types[j+1]; types[j+1] = tmpType;
                }
            }
        }
    }

    /** Ordenacion descendente — PDF: sort(z-a). */
    public void sortDescending() {
        sortAscending();
        // Invertir el array ya ordenado
        for (int i = 0, j = size-1; i < j; i++, j--) {
            double t = values[i]; values[i] = values[j]; values[j] = t;
        }
    }

    /** Promedio de todos los valores. */
    public double average() {
        if (size == 0) return 0;
        double sum = 0;
        for (int i = 0; i < size; i++) sum += values[i];
        return sum / size;
    }

    /** Redimension automatica cuando el array se llena — PDF Array Dinamico. */
    private void resize() {
        double[] newValues = new double[values.length * 2];
        SensorType[] newTypes = new SensorType[types.length * 2];
        System.arraycopy(values, 0, newValues, 0, size);
        System.arraycopy(types,  0, newTypes,  0, size);
        values = newValues;
        types  = newTypes;
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Indice " + index + " fuera de rango [0," + size + ")");
    }

    public int size()      { return size; }
    public boolean isEmpty(){ return size == 0; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SensorArray[");
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%.1f(%s)", values[i], types[i]));
            if (i < size-1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}