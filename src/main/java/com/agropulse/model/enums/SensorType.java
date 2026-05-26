package com.agropulse.model.enums;

/**
 * Tipos de sensores disponibles en el invernadero.
 * Usado por el Patrón Abstract Factory para crear familias de sensores.
 */
public enum SensorType {
    TEMPERATURE,          // Temperatura en °C
    HUMIDITY,             // Humedad relativa en %
    SOIL_MOISTURE,        // Humedad del suelo en %
    LIGHT,                // Luminosidad en lux
    CO2,                  // CO₂ en ppm
    PH,                   // pH del suelo
    WIND_SPEED,           // Velocidad del viento (sensores exteriores)
    TEMPERATURE_INTERNAL, // Temperatura interior del invernadero
    TEMPERATURE_EXTERNAL, // Temperatura exterior del invernadero
    HUMIDITY_INTERNAL,    // Humedad relativa interior del invernadero
    HUMIDITY_EXTERNAL     // Humedad relativa exterior del invernadero
}
