package com.agropulse.pattern.behavioral.state;

import com.agropulse.model.enums.CropStage;

/** Estado: COSECHA. Los frutos están maduros y listos para recolectar. */
public class HarvestingState implements ICropState {

    @Override
    public void advanceToNextStage(CropContext ctx) {
        ctx.setState(new DormantState(), CropStage.DORMANT);
    }

    @Override
    public boolean canAdvance(CropContext ctx) { return true; } // Siempre puede pasar a reposo

    @Override
    public String getCareRecommendations() {
        return "Cosechar en horas frescas (mañana). Reducir riego. Temperatura 18-24°C. Preparar almacenamiento frío.";
    }

    @Override
    public double[] getOptimalTemperatureRange() { return new double[]{18.0, 24.0}; }

    @Override
    public double[] getOptimalHumidityRange()    { return new double[]{45.0, 60.0}; }

    @Override
    public String getStateName() { return "Cosecha (Harvesting)"; }
}
