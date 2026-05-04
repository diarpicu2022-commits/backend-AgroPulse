package com.agropulse.pattern.behavioral.state;

import com.agropulse.model.enums.CropStage;

/** Estado: FLORACIÓN. Etapa crítica donde se forman los frutos. */
public class FloweringState implements ICropState {
    private static final int MIN_DAYS_BEFORE_HARVESTING = 14;

    @Override
    public void advanceToNextStage(CropContext ctx) {
        ctx.setState(new HarvestingState(), CropStage.HARVESTING);
    }

    @Override
    public boolean canAdvance(CropContext ctx) {
        return ctx.getDaysInCurrentStage() >= MIN_DAYS_BEFORE_HARVESTING;
    }

    @Override
    public String getCareRecommendations() {
        return "ETAPA CRÍTICA: Temperatura exacta 22-26°C. Humedad 50-65%. Riego reducido. Potasio y fósforo. No disturbar plantas.";
    }

    @Override
    public double[] getOptimalTemperatureRange() { return new double[]{22.0, 26.0}; }

    @Override
    public double[] getOptimalHumidityRange()    { return new double[]{50.0, 65.0}; }

    @Override
    public String getStateName() { return "Floración (Flowering)"; }
}
