package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rules")
public class Rule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false) private String name;
    private String sensor;        // sensor type trigger
    private String condition;     // GT, LT, EQ
    @Column(name = "condition_value") private double conditionValue;
    private String action;        // action description
    @Column(name = "actuator_id") private int actuatorId;
    private boolean active;
    @Column(name = "created_at") private LocalDateTime createdAt;

    public Rule() { this.active = true; this.createdAt = LocalDateTime.now(); }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getName()                     { return name; }
    public void setName(String v)               { this.name = v; }
    public String getSensor()                   { return sensor; }
    public void setSensor(String v)             { this.sensor = v; }
    public String getCondition()                { return condition; }
    public void setCondition(String v)          { this.condition = v; }
    public double getConditionValue()           { return conditionValue; }
    public void setConditionValue(double v)     { this.conditionValue = v; }
    public String getAction()                   { return action; }
    public void setAction(String v)             { this.action = v; }
    public int getActuatorId()                  { return actuatorId; }
    public void setActuatorId(int v)            { this.actuatorId = v; }
    public boolean isActive()                   { return active; }
    public void setActive(boolean v)            { this.active = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }

    @Override public String toString() {
        return String.format("Rule{id=%d, name='%s', sensor=%s, condition=%s}", id, name, sensor, condition);
    }
}
