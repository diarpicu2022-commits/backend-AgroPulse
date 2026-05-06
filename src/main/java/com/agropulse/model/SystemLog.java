package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
public class SystemLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false) private String action;
    @Column(name = "user_name") private String userName;
    @Column(columnDefinition = "TEXT") private String details;
    private String level;   // INFO, WARNING, ERROR
    private LocalDateTime timestamp;

    public SystemLog() { this.timestamp = LocalDateTime.now(); this.level = "INFO"; }
    public SystemLog(String action, String userName, String details) {
        this(); this.action = action; this.userName = userName; this.details = details;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getAction()                   { return action; }
    public void setAction(String v)             { this.action = v; }
    public String getUserName()                 { return userName; }
    public void setUserName(String v)           { this.userName = v; }
    public String getDetails()                  { return details; }
    public void setDetails(String v)            { this.details = v; }
    public String getLevel()                    { return level; }
    public void setLevel(String v)              { this.level = v; }
    public LocalDateTime getTimestamp()         { return timestamp; }
    public void setTimestamp(LocalDateTime v)   { this.timestamp = v; }

    @Override public String toString() {
        return String.format("SystemLog{id=%d, action='%s', user='%s', level=%s}", id, action, userName, level);
    }
}
