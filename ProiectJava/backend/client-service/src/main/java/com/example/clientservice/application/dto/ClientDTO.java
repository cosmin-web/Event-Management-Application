package com.example.clientservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class ClientDTO {

    private String id;

    @Email(message = "Formatul email-ului este invalid.")
    @NotBlank(message = "Email-ul este obligatoriu.")
    @Size(max = 100, message = "Email-ul nu poate depasi 100 de caractere.")
    private String email;

    @Size(max = 50, message = "Numele nu poate depasi 50 de caractere.")
    private String nume;

    @Size(max = 50, message = "Prenumele nu poate depasi 50 de caractere.")
    private String prenume;

    private Boolean isPublic;
    private Map<String, String> social;
    private List<TicketDTO> bilete;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Map<String, String> getSocial() { return social; }
    public void setSocial(Map<String, String> social) { this.social = social; }

    public List<TicketDTO> getBilete() { return bilete; }
    public void setBilete(List<TicketDTO> bilete) { this.bilete = bilete; }
}
