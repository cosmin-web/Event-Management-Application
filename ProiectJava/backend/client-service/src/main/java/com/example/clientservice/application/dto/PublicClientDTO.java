package com.example.clientservice.application.dto;

import java.util.List;

public class PublicClientDTO {

    private String id;
    private String email;
    private String nume;
    private String prenume;
    private List<TicketDTO> bilete;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }

    public List<TicketDTO> getBilete() { return bilete; }
    public void setBilete(List<TicketDTO> bilete) { this.bilete = bilete; }
}
