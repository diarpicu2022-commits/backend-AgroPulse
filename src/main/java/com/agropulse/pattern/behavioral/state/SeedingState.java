package com.agropulse.pattern.behavioral.state;

import com.agropulse.model.enums.CropStage;

/** Estado: SIEMBRA. El cultivo acaba de ser sembrado, espera germinación. */
public class SeedingState implements ICropState {
    private static final int MIN_DAYS_BEFORE_GROWING = 7; // Días mínimos de germinación

    @Override
    public void advanceToNextStage(CropContext ctx) {
        ctx.setState(new GrowingState(), CropStage.GROWING);
    }

    @Override
    public boolean canAdvance(CropContext ctx) {
        return ctx.getDaysInCurrentStage() >= MIN_DAYS_BEFORE_GROWING;
    }

    @Override
    public String getCareRecommendations() {
        return "Mantener suelo húmedo (70-80%). Temperatura 18-24°C. Evitar luz solar directa intensa. Riego suave 2 veces al día.";
    }

    @Override
    public double[] getOptimalTemperatureRange() { return new double[]{18.0, 24.0}; }

    @Override
    public double[] getOptimalHumidityRange()    { return new double[]{70.0, 80.0}; }

    @Override
    public String getStateName() { return "Siembra (Seeding)"; }
}
