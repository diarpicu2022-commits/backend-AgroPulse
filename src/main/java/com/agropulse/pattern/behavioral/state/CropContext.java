package com.agropulse.pattern.behavioral.state;

import com.agropulse.model.Crop;
import com.agropulse.model.enums.CropStage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Contexto del Patrón State: representa un cultivo con su estado actual.
 * El cliente interactúa con el Context, no con los estados directamente.
 */
public class CropContext {

    private ICropState currentState;   // Estado actual del cultivo
    private final Crop crop;           // Datos del cultivo (modelo de dominio)
    private LocalDate stageStartDate;  // Fecha en que inició el estado actual

    public CropContext(Crop crop) {
        this.crop           = crop;
        this.stageStartDate = LocalDate.now();
        // Inicializar estado según la etapa actual del cultivo
        this.currentState   = createStateFromStage(crop.getCurrentStage());
    }

    /** Crea la instancia de estado correcta para la etapa dada. */
    private ICropState createStateFromStage(CropStage stage) {
        return switch (stage) {
            case SEEDING    -> new SeedingState();
            case GROWING    -> new GrowingState();
            case FLOWERING  -> new FloweringState();
            case HARVESTING -> new HarvestingState();
            case DORMANT    -> new DormantState();
        };
    }

    /** Llamado por los estados concretos para cambiar el estado actual. */
    public void setState(ICropState newState, CropStage newStage) {
        System.out.printf("[State] Cultivo '%s': %s → %s%n",
                crop.getName(), currentState.getStateName(), newState.getStateName());
        this.currentState   = newState;
        this.stageStartDate = LocalDate.now();
        this.crop.setCurrentStage(newStage);
    }

    /** Solicita al estado actual que avance al siguiente. */
    public void advance() {
        if (currentState.canAdvance(this)) {
            currentState.advanceToNextStage(this);
        } else {
            System.out.printf("[State] Cultivo '%s' no puede avanzar aún: %s%n",
                    crop.getName(), currentState.getStateName());
        }
    }

    /** Días transcurridos en el estado actual. */
    public long getDaysInCurrentStage() {
        return ChronoUnit.DAYS.between(stageStartDate, LocalDate.now());
    }

    public ICropState getCurrentState()   { return currentState; }
    public Crop getCrop()                 { return crop; }
    public LocalDate getStageStartDate()  { return stageStartDate; }
}
