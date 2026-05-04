package com.agropulse.pattern.behavioral.state;

import com.agropulse.model.enums.CropStage;

/** Estado: REPOSO. El cultivo terminó su ciclo, el invernadero se prepara para el siguiente. */
public class DormantState implements ICropState {

    @Override
    public void advanceToNextStage(CropContext ctx) {
        // Reinicia el ciclo: vuelve a siembra (nuevo cultivo)
        ctx.setState(new SeedingState(), CropStage.SEEDING);
    }

    @Override
    public boolean canAdvance(CropContext ctx) { return true; }

    @Override
    public String getCareRecommendations() {
        return "Desinfectar invernadero. Remover residuos vegetales. Reacondicionado de suelo. Planificación del siguiente ciclo.";
    }

    @Override
    public double[] getOptimalTemperatureRange() { return new double[]{10.0, 20.0}; }

    @Override
    public double[] getOptimalHumidityRange()    { return new double[]{30.0, 50.0}; }

    @Override
    public String getStateName() { return "Reposo (Dormant)"; }
}
