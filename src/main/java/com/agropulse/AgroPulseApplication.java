package com.agropulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  AgroPulse — Sistema IoT de Monitoreo de Invernaderos              ║
 * ║  Versión 2.0 — Refactorización completa                            ║
 * ║                                                                    ║
 * ║  Patrones implementados (GoF):                                     ║
 * ║   Creacionales : Singleton, Factory Method, Abstract Factory,      ║
 * ║                  Prototype, Builder                                ║
 * ║   Estructurales: Adapter, Bridge, Decorator, Facade, Proxy         ║
 * ║   Comportamiento: State, Observer                                  ║
 * ║                                                                    ║
 * ║  Estructuras de datos:                                             ║
 * ║   Arrays (1D, 2D, circular), Pila (LIFO), Cola (FIFO),            ║
 * ║   Lista simple enlazada, Lista doble enlazada,                     ║
 * ║   Lista circular doble                                             ║
 * ║                                                                    ║
 * ║  Principios POO: Encapsulamiento, Herencia, Polimorfismo,          ║
 * ║                  Abstracción, Interfaces, Clases abstractas,       ║
 * ║                  Modificadores de acceso                           ║
 * ║                                                                    ║
 * ║  Principios SOLID: SRP, OCP, LSP, ISP, DIP                        ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
public class AgroPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroPulseApplication.class, args);
    }
}
