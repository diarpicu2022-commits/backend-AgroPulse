package com.agropulse.structure.queue;
/** Interfaz de la Cola (Queue). PDF Colas: enqueue, dequeue, front, rear, size. */
public interface IQueue<T> {
    void enqueue(T item);   // Insertar al final
    T dequeue();            // Extraer del frente (FIFO)
    T front();              // Ver frente sin extraer
    T rear();               // Ver final sin extraer
    boolean isEmpty();
    int size();
}