package com.agropulse.model.enums;

/**
 * Etapas del ciclo de vida de un cultivo.
 * Usadas por el Patrón State para modelar las transiciones del cultivo.
 *
 * Flujo normal: SEEDING → GROWING → FLOWERING → HARVESTING → DORMANT
 */
public enum CropStage {
    SEEDING,     // Siembra
    GROWING,     // Crecimiento vegetativo
    FLOWERING,   // Floración
    HARVESTING,  // Cosecha
    DORMANT      // Reposo / fuera de temporada
}
