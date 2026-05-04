package com.agropulse.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz generica del patron DAO.
 * Principios POO (Bloque 10 — Interfaces):
 *   "Una interfaz es una clase abstracta completa que define un contrato".
 *
 * Principio Interface Segregation (ISP — SOLID):
 *   Los DAOs concretos solo implementan las operaciones que necesitan.
 *
 * @param <T>  Tipo de entidad de dominio
 * @param <ID> Tipo del identificador primario
 */
public interface GenericDao<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(ID id);
    boolean existsById(ID id);
}
