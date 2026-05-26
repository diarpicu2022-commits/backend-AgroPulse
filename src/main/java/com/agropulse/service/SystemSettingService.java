package com.agropulse.service;

import com.agropulse.dao.SystemSettingRepository;
import com.agropulse.model.SystemSetting;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository repository;

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void seed() {
        seedIfAbsent("ai.provider",      "groq",           "Proveedor de IA: groq | github");
        seedIfAbsent("ai.model",         "llama3-8b-8192", "Modelo de lenguaje a usar");
        seedIfAbsent("ai.enabled",       "true",           "Activar/desactivar asistente de IA");
        seedIfAbsent("alerts.email",     "true",           "Enviar alertas por correo electrónico");
        seedIfAbsent("alerts.whatsapp",  "false",          "Enviar alertas por WhatsApp");
        refreshCache();
    }

    private void seedIfAbsent(String key, String defaultValue, String description) {
        if (repository.findByKey(key).isEmpty()) {
            repository.save(new SystemSetting(key, defaultValue, description));
        }
    }

    private void refreshCache() {
        repository.findAll().forEach(s -> cache.put(s.getKey(), s.getValue()));
    }

    public String get(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = cache.get(key);
        return val != null ? Boolean.parseBoolean(val) : defaultValue;
    }

    public List<SystemSetting> getAll() {
        return repository.findAll();
    }

    public SystemSetting upsert(String key, String value) {
        Optional<SystemSetting> opt = repository.findByKey(key);
        SystemSetting setting = opt.orElseGet(() -> new SystemSetting(key, value, null));
        setting.setValue(value);
        SystemSetting saved = repository.save(setting);
        cache.put(key, value);
        return saved;
    }
}
