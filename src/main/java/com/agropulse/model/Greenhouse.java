package com.agropulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Invernadero — entidad principal del sistema AgroPulse. Usada por GreenhouseFacade. */
@Entity
@Table(name = "greenhouses")
public class Greenhouse implements Cloneable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false) private String name;
    private String location;
    private String description;
    @Column(name = "owner_id")  private int ownerId;
    private boolean active;
    @Column(name = "created_at") private LocalDateTime createdAt;

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Column(name = "photo_url", nullable = true, length = 500)
    private String photoUrl;

    public Greenhouse() { this.active = true; this.createdAt = LocalDateTime.now(); }
    public Greenhouse(String name, String location, String description, int ownerId) {
        this(); this.name = name; this.location = location;
        this.description = description; this.ownerId = ownerId;
    }

    public int getId()                             { return id; }
    public void setId(int id)                      { this.id = id; }
    public String getName()                        { return name; }
    public void setName(String v)                  { this.name = v; }
    public String getLocation()                    { return location; }
    public void setLocation(String v)              { this.location = v; }
    public String getDescription()                 { return description; }
    public void setDescription(String v)           { this.description = v; }
    public int getOwnerId()                        { return ownerId; }
    public void setOwnerId(int v)                  { this.ownerId = v; }
    public boolean isActive()                      { return active; }
    public void setActive(boolean v)               { this.active = v; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime v)      { this.createdAt = v; }
    public Double getLatitude()                    { return latitude; }
    public void setLatitude(Double v)              { this.latitude = v; }
    public Double getLongitude()                   { return longitude; }
    public void setLongitude(Double v)             { this.longitude = v; }
    public String getPhotoUrl()                    { return photoUrl; }
    public void setPhotoUrl(String v)              { this.photoUrl = v; }

    @Override
    public Greenhouse clone() {
        try {
            return (Greenhouse) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar invernadero", e);
        }
    }

    @Override public String toString() {
        return String.format("Greenhouse{id=%d, name='%s', location='%s'}", id, name, location);
    }
}
