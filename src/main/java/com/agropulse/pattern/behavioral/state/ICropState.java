package com.agropulse.pattern.behavioral.state;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PATRÓN STATE — Ciclo de vida del cultivo                   ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  Permite a un cultivo alterar su comportamiento cuando su   ║
 * ║  estado interno (etapa del ciclo) cambia.                   ║
 * ║                                                             ║
 * ║  Flujo de estados:                                          ║
 * ║   SEEDING → GROWING → FLOWERING → HARVESTING → DORMANT     ║
 * ║                                                             ║
 * ║  Analogía del PDF State:                                    ║
 * ║   Como un pedido de restaurante: Pendiente → Preparando    ║
 * ║   → Listo → Entregado. Cada estado tiene comportamiento     ║
 * ║   distinto ante las mismas acciones.                        ║
 * ║                                                             ║
 * ║  Ventaja (SRP + OCP):                                       ║
 * ║   Elimina los if/else if gigantes que verifican la etapa    ║
 * ║   del cultivo en múltiples métodos.                         ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public interface ICropState {

    /**
     * Acción de avanzar al siguiente estado en el ciclo de vida.
     * Cada estado concreto decide cuál es el siguiente.
     *
     * @param context El contexto (cultivo) que cambia de estado
     */
    void advanceToNextStage(CropContext context);

    /**
     * Obtiene las recomendaciones de cuidado para esta etapa.
     * Polimorfismo: cada estado tiene recomendaciones distintas.
     */
    String getCareRecommendations();

    /**
     * Obtiene los rangos de temperatura óptimos para esta etapa.
     * Los rangos varían según la fase del cultivo.
     */
    double[] getOptimalTemperatureRange();

    /**
     * Obtiene los rangos de humedad óptimos para esta etapa.
     */
    double[] getOptimalHumidityRange();

    /**
     * Nombre legible del estado actual.
     */
    String getStateName();

    /**
     * Verifica si el cultivo puede avanzar al siguiente estado.
     * Ejemplo: no puede pasar de SEEDING a GROWING si aún no han
     * transcurrido los días mínimos de germinación.
     */
    boolean canAdvance(CropContext context);
}
