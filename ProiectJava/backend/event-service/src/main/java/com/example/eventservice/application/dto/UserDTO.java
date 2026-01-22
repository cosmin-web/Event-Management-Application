package com.example.eventservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO {
    private Integer id;

    @Email(message = "Emailul este invalid")
    @NotBlank(message = "Emailul este obligatoriu")
    @Size(max = 100, message = "Emailul este prea lung (max 100).")
    private String email;

    @NotBlank(message = "Rolul este obligatoriu")
    private String rol;

    //@NotBlank(message = "Parola este obligatorie")
    @Size(min = 6, max = 50, message = "Parola trebuie sa aiba intre 6 si 50 de caractere.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String parola;

    private String nume;
    private String prenume;

    public UserDTO() {}

    public UserDTO(Integer id, String email, String rol) {
        this.id = id;
        this.email = email;
        this.rol = rol;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getParola() { return parola; }
    public void setParola(String parola) { this.parola = parola; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }
}