package com.agropulse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alert_recipients")
public class AlertRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "greenhouse_id", nullable = false)
    private int greenhouseId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "callmebot_apikey", length = 60)
    private String callmebotApikey;

    @Column(nullable = false)
    private boolean active = true;

    public int getId()                         { return id; }
    public void setId(int id)                  { this.id = id; }
    public int getGreenhouseId()               { return greenhouseId; }
    public void setGreenhouseId(int v)         { this.greenhouseId = v; }
    public String getName()                    { return name; }
    public void setName(String v)              { this.name = v; }
    public String getEmail()                   { return email; }
    public void setEmail(String v)             { this.email = v; }
    public String getPhone()                   { return phone; }
    public void setPhone(String v)             { this.phone = v; }
    public String getCallmebotApikey()         { return callmebotApikey; }
    public void setCallmebotApikey(String v)   { this.callmebotApikey = v; }
    public boolean isActive()                  { return active; }
    public void setActive(boolean v)           { this.active = v; }
}
