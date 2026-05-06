package com.agropulse.model;

import com.agropulse.model.enums.AlertLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Alerta generada por el sistema cuando un sensor supera sus umbrales. Almacenada en AlertStack. */
@Entity
@Table(name = "alerts")
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AlertLevel level;
    private boolean sent;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "greenhouse_id") private int greenhouseId;

    // New fields
    private String type;
    private String title;
    @Column(name = "read_at") private boolean read;

    public Alert() { this.createdAt = LocalDateTime.now(); this.sent = false; this.read = false; }
    public Alert(String message, AlertLevel level, int greenhouseId) {
        this(); this.message = message; this.level = level; this.greenhouseId = greenhouseId;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getMessage()                  { return message; }
    public void setMessage(String v)            { this.message = v; }
    public AlertLevel getLevel()                { return level; }
    public void setLevel(AlertLevel v)          { this.level = v; }
    public boolean isSent()                     { return sent; }
    public void setSent(boolean v)              { this.sent = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public int getGreenhouseId()                { return greenhouseId; }
    public void setGreenhouseId(int v)          { this.greenhouseId = v; }
    public String getType()                     { return type; }
    public void setType(String v)               { this.type = v; }
    public String getTitle()                    { return title; }
    public void setTitle(String v)              { this.title = v; }
    public boolean isRead()                     { return read; }
    public void setRead(boolean v)              { this.read = v; }

    @Override public String toString() {
        return String.format("Alert{[%s] %s}", level, message);
    }
}
