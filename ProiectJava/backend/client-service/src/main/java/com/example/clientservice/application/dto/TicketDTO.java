package com.example.clientservice.application.dto;

import jakarta.validation.constraints.Size;

public class TicketDTO {

    @Size(max = 64, message = "Codul biletului prea lung")
    private String cod;

    @Size(max = 50)
    private String tip;

    private Integer eventId;
    private Integer packageId;

    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public Integer getPackageId() { return packageId; }
    public void setPackageId(Integer packageId) { this.packageId = packageId; }
}
