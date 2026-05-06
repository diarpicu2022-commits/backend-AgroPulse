package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    private String status;    // OPEN, IN_PROGRESS, CLOSED
    private String priority;  // LOW, MEDIUM, HIGH
    @Column(name = "user_id") private int userId;
    @Column(name = "user_name") private String userName;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    public Ticket() {
        this.status = "OPEN";
        this.priority = "MEDIUM";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public String getTitle()                    { return title; }
    public void setTitle(String v)              { this.title = v; }
    public String getDescription()              { return description; }
    public void setDescription(String v)        { this.description = v; }
    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }
    public String getPriority()                 { return priority; }
    public void setPriority(String v)           { this.priority = v; }
    public int getUserId()                      { return userId; }
    public void setUserId(int v)                { this.userId = v; }
    public String getUserName()                 { return userName; }
    public void setUserName(String v)           { this.userName = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }

    @Override public String toString() {
        return String.format("Ticket{id=%d, title='%s', status=%s, priority=%s}", id, title, status, priority);
    }
}
