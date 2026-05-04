package com.agropulse.structure.stack;
/** Interfaz de la Pila (Stack). PDF Pilas: operaciones push, pop, top, isEmpty. */
public interface IStack<T> {
    void push(T item);    // Insertar en la cima
    T pop();              // Extraer de la cima (LIFO)
    T peek();             // Ver cima sin extraer
    boolean isEmpty();
    int size();
}