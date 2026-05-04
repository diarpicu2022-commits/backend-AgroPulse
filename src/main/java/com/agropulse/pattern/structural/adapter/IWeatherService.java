package com.agropulse.pattern.structural.adapter;
public interface IWeatherService {
    double getExternalTemperature(String city);
    double getExternalHumidity(String city);
    boolean willRainToday(String city);
}