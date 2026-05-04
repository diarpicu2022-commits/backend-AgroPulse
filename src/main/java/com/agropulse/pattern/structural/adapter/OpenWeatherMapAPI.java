package com.agropulse.pattern.structural.adapter;
/** Adaptee: API externa con interfaz incompatible. */
public class OpenWeatherMapAPI {
    public double fetchTempCelsius(double lat, double lon) { return 18.5; }
    public int fetchHumidityPercent(double lat, double lon) { return 72; }
    public String fetchWeatherCondition(double lat, double lon) { return "PARTLY_CLOUDY"; }
}