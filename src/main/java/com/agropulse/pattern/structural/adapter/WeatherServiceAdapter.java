package com.agropulse.pattern.structural.adapter;
/**
 * PATRON ADAPTER: adapta OpenWeatherMapAPI a IWeatherService.
 * Permite integrar servicios externos con interfaces incompatibles sin modificar el codigo existente.
 */
public class WeatherServiceAdapter implements IWeatherService {
    private static final double PASTO_LAT = 1.2136;
    private static final double PASTO_LON = -77.2811;
    private final OpenWeatherMapAPI externalApi = new OpenWeatherMapAPI();
    @Override public double getExternalTemperature(String city) { return externalApi.fetchTempCelsius(PASTO_LAT, PASTO_LON); }
    @Override public double getExternalHumidity(String city) { return externalApi.fetchHumidityPercent(PASTO_LAT, PASTO_LON); }
    @Override public boolean willRainToday(String city) { String c=externalApi.fetchWeatherCondition(PASTO_LAT,PASTO_LON); return c.contains("RAIN")||c.contains("STORM"); }
}