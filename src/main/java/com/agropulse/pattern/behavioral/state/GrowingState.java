package com.agropulse.pattern.behavioral.state;

import com.agropulse.model.enums.CropStage;

/** Estado: CRECIMIENTO vegetativo. El cultivo está desarrollando hojas y tallo. */
public class GrowingState implements ICropState {
    private static final int MIN_DAYS_BEFORE_FLOWERING = 21;

    @Override
    public void advanceToNextStage(CropContext ctx) {
        ctx.setState(new FloweringState(), CropStage.FLOWERING);
    }

    @Override
    public boolean canAdvance(CropContext ctx) {
        return ctx.getDaysInCurrentStage() >= MIN_DAYS_BEFORE_FLOWERING;
    }

    @Override
    public String getCareRecommendations() {
        return "Riego regular cada 2 días. Fertilización nitrogenada. Temperatura 20-28°C. Humedad 60-70%. Ventilación adecuada.";
    }

    @Override
    public double[] getOptimalTemperatureRange() { return new double[]{20.0, 28.0}; }

    @Override
    public double[] getOptimalHumidityRange()    { return new double[]{60.0, 70.0}; }

    @Override
    public String getStateName() { return "Crecimiento (Growing)"; }
}
