package com.agropulse.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GreenhouseCreateDto {

    @NotBlank(message = "name es obligatorio")
    @Size(max = 100, message = "name no puede superar 100 caracteres")
    private String name;

    private String location;
    private String description;
    private Integer ownerId;
    private Boolean active;
    private Double latitude;
    private Double longitude;
    private String photoUrl;

    public String getName()        { return name; }
    public String getLocation()    { return location; }
    public String getDescription() { return description; }
    public Integer getOwnerId()    { return ownerId; }
    public Boolean getActive()     { return active; }
    public Double getLatitude()    { return latitude; }
    public Double getLongitude()   { return longitude; }
    public String getPhotoUrl()    { return photoUrl; }

    public void setName(String name)               { this.name = name; }
    public void setLocation(String location)       { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerId(Integer ownerId)        { this.ownerId = ownerId; }
    public void setActive(Boolean active)          { this.active = active; }
    public void setLatitude(Double latitude)       { this.latitude = latitude; }
    public void setLongitude(Double longitude)     { this.longitude = longitude; }
    public void setPhotoUrl(String photoUrl)       { this.photoUrl = photoUrl; }
}
