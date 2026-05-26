package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SystemSetting() {}

    public SystemSetting(String key, String value, String description) {
        this.key         = key;
        this.value       = value;
        this.description = description;
        this.updatedAt   = LocalDateTime.now();
    }

    public int             getId()                          { return id; }
    public void            setId(int id)                   { this.id = id; }
    public String          getKey()                        { return key; }
    public void            setKey(String key)              { this.key = key; }
    public String          getValue()                      { return value; }
    public void            setValue(String value)          { this.value = value; this.updatedAt = LocalDateTime.now(); }
    public String          getDescription()                { return description; }
    public void            setDescription(String d)        { this.description = d; }
    public LocalDateTime   getUpdatedAt()                  { return updatedAt; }
    public void            setUpdatedAt(LocalDateTime t)   { this.updatedAt = t; }
}
