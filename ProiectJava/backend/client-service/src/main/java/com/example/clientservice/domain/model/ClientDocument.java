package com.example.clientservice.domain.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "clients")
public class ClientDocument {

    @Id
    private ObjectId id;

    private String email;
    private String nume;
    private String prenume;
    private boolean isPublic;
    private Map<String, String> social = new HashMap<>();

    private List<TicketRef> bilete = new ArrayList<>();

    public ClientDocument() {}

    public String getId() { return id != null ? id.toHexString() : null;}
    public void setId(ObjectId id) { this.id = id; }
    public String getIdAsString() {
        return id != null ? id.toHexString() : null;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public Map<String, String> getSocial() { return social; }
    public void setSocial(Map<String, String> social) { this.social = social; }

    public List<TicketRef> getBilete() { return bilete; }
    public void setBilete(List<TicketRef> bilete) { this.bilete = bilete; }
}
