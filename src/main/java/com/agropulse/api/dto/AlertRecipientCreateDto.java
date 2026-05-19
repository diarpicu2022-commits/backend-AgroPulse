package com.agropulse.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AlertRecipientCreateDto {

    @NotBlank(message = "name es obligatorio")
    @Size(max = 100, message = "name no puede superar 100 caracteres")
    private String name;

    @Email(message = "email no es válido")
    private String email;

    @Pattern(regexp = "^\\+\\d{7,15}$", message = "phone debe ser un número internacional (ej: +573001234567)")
    private String phone;

    private String callmebotApikey;

    public String getName()             { return name; }
    public String getEmail()            { return email; }
    public String getPhone()            { return phone; }
    public String getCallmebotApikey()  { return callmebotApikey; }

    public void setName(String name)                        { this.name = name; }
    public void setEmail(String email)                      { this.email = email; }
    public void setPhone(String phone)                      { this.phone = phone; }
    public void setCallmebotApikey(String callmebotApikey)  { this.callmebotApikey = callmebotApikey; }
}
